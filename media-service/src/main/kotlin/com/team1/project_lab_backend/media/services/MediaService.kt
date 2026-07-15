package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.dto.MediaResponse
import com.team1.project_lab_backend.media.dto.UpdateMediaRequest
import com.team1.project_lab_backend.media.models.Media
import com.team1.project_lab_backend.media.models.MediaOwnerType
import com.team1.project_lab_backend.media.repositories.MediaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

/**
 * Ownership (does the requesting user own the Stay/Room/User this media belongs
 * to) is the Gateway's job (docs/adr/0005) — it's the only place that still has
 * local Stay/Room repositories until Phases 4-5 extract Identity/Inventory. This
 * service only knows about media rows and files: existence-under-owner and the
 * one-primary-per-owner invariant.
 */
@Service
class MediaService(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService,
) {
    @Transactional(readOnly = true)
    fun listForOwner(ownerType: MediaOwnerType, ownerId: Int): List<MediaResponse> =
        mediaRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun listForOwners(ownerType: MediaOwnerType, ownerIds: Collection<Int>): List<MediaResponse> =
        mediaRepository.findByOwnerTypeAndOwnerIdIn(ownerType, ownerIds).map { it.toResponse() }

    @Transactional
    fun addMedia(
        ownerType: MediaOwnerType,
        ownerId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
    ): MediaResponse {
        if (displayOrder < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        validateImageFile(file)
        if (isPrimary && mediaRepository.existsByOwnerTypeAndOwnerIdAndIsPrimary(ownerType, ownerId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this ${ownerType.folderNoun()}")
        }
        val key = storageService.save(file, "${ownerType.folderNoun()}s/$ownerId")
        val media = Media(
            ownerType = ownerType, ownerId = ownerId, url = key,
            caption = caption, isPrimary = isPrimary, displayOrder = displayOrder,
        )
        try {
            return mediaRepository.save(media).toResponse()
        } catch (e: Exception) {
            runCatching { storageService.delete(key) }
            throw e
        }
    }

    @Transactional
    fun updateMedia(ownerType: MediaOwnerType, ownerId: Int, id: Int, request: UpdateMediaRequest): MediaResponse {
        if (request.displayOrder < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        val existing = mediaRepository.findByOwnerTypeAndOwnerIdAndId(ownerType, ownerId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        if (request.isPrimary && !existing.isPrimary &&
            mediaRepository.existsByOwnerTypeAndOwnerIdAndIsPrimary(ownerType, ownerId, true)
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this ${ownerType.folderNoun()}")
        }
        return mediaRepository.save(
            existing.copy(caption = request.caption, isPrimary = request.isPrimary, displayOrder = request.displayOrder),
        ).toResponse()
    }

    @Transactional
    fun deleteMedia(ownerType: MediaOwnerType, ownerId: Int, id: Int) {
        val existing = mediaRepository.findByOwnerTypeAndOwnerIdAndId(ownerType, ownerId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        mediaRepository.deleteById(id)
        storageService.delete(existing.url)
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty")
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must be an image (got: $contentType)")
        }
        val ext = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: ""
        if (ext !in ALLOWED_EXTENSIONS) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported image extension: .$ext")
        }
    }

    private fun MediaOwnerType.folderNoun(): String = name.lowercase()

    private fun Media.toResponse() = MediaResponse(
        id = id,
        ownerType = ownerType.name,
        ownerId = ownerId,
        url = storageService.toUrl(url),
        caption = caption,
        isPrimary = isPrimary,
        displayOrder = displayOrder,
    )

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "avif")
    }
}
