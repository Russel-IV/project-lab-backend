package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.models.StayPicture
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Service
class StayPictureService(
    private val stayPictureRepository: StayPictureRepository,
    private val stayRepository: StayRepository,
    private val storageService: StorageService,
) {
    @Transactional(readOnly = true)
    fun getPicturesForStay(stayId: Int): List<StayPictureResponse> {
        stayId.requirePositive("stayId")
        return stayPictureRepository.findByStayId(stayId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getPicturesForStayAsEntities(stayId: Int): List<StayPicture> {
        stayId.requirePositive("stayId")
        return stayPictureRepository.findByStayId(stayId)
    }

    fun resolveUrl(stayPicture: StayPicture): String = storageService.toUrl(stayPicture.url)

    @Transactional
    fun addPicture(
        stayId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): StayPictureResponse {
        stayId.requirePositive("stayId")
        val stay = stayRepository.findById(stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        displayOrder.requireNonNegative("displayOrder")
        validateImageFile(file)
        if (isPrimary && stayPictureRepository.existsByStayIdAndIsPrimary(stayId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this stay")
        }
        val key = storageService.save(file, stayId)
        val picture = StayPicture(
            id = 0, stayId = stayId, url = key,
            caption = caption, isPrimary = isPrimary, displayOrder = displayOrder,
        )
        try {
            return stayPictureRepository.save(picture).toResponse()
        } catch (e: Exception) {
            runCatching { storageService.delete(key) }
            throw e
        }
    }

    @Transactional
    fun updatePictureMetadata(
        stayId: Int,
        id: Int,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): StayPicture {
        stayId.requirePositive("stayId")
        id.requirePositive()
        displayOrder.requireNonNegative("displayOrder")
        val stay = stayRepository.findById(stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        val existing = stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        if (isPrimary && !existing.isPrimary && stayPictureRepository.existsByStayIdAndIsPrimary(stayId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this stay")
        }
        return stayPictureRepository.save(
            StayPicture(
                id = id,
                stayId = stayId,
                url = existing.url,
                caption = caption,
                isPrimary = isPrimary,
                displayOrder = displayOrder,
            ),
        )
    }

    @Transactional
    fun deletePicture(stayId: Int, id: Int, requestingUserId: Int) {
        stayId.requirePositive("stayId")
        id.requirePositive()
        val stay = stayRepository.findById(stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        val existing = stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        stayPictureRepository.deleteById(id)
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

    private fun StayPicture.toResponse(): StayPictureResponse =
        StayPictureResponse(
            id = id,
            stayId = stayId,
            url = storageService.toUrl(url),
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "avif")
    }
}
