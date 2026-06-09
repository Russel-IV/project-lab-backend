package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.models.StayPicture
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
class StayPictureService(
    private val stayPictureRepository: StayPictureRepository,
    private val stayRepository: StayRepository,
    @Value("\${app.upload.dir}") private val uploadDir: String
) {
    @Transactional(readOnly = true)
    fun getPicturesForStay(stayId: Int): List<StayPictureResponse> {
        if (stayId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        return stayPictureRepository.findByStayId(stayId).map { it.toResponse() }
    }

    @Transactional
    fun addPicture(
        stayId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int
    ): StayPictureResponse {
        if (stayId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        if (!stayRepository.existsById(stayId)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        if (displayOrder < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        validateImageFile(file)
        if (isPrimary && stayPictureRepository.existsByStayIdAndIsPrimary(stayId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this stay")
        }
        val url = saveFile(file, stayId)
        val picture = StayPicture(
            id = 0, stayId = stayId, url = url,
            caption = caption, isPrimary = isPrimary, displayOrder = displayOrder
        )
        return stayPictureRepository.save(picture).toResponse()
    }

    @Transactional
    fun updatePicture(
        stayId: Int,
        id: Int,
        file: MultipartFile?,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int
    ): StayPictureResponse {
        if (stayId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        if (id <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        if (displayOrder < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        val existing = stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        if (isPrimary && !existing.isPrimary && stayPictureRepository.existsByStayIdAndIsPrimary(stayId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this stay")
        }
        val url = if (file != null && !file.isEmpty) {
            validateImageFile(file)
            deleteFile(existing.url)
            saveFile(file, stayId)
        } else {
            existing.url
        }
        val updated = StayPicture(
            id = id, stayId = stayId, url = url,
            caption = caption, isPrimary = isPrimary, displayOrder = displayOrder
        )
        return stayPictureRepository.save(updated).toResponse()
    }

    @Transactional
    fun deletePicture(stayId: Int, id: Int) {
        if (stayId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        if (id <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        val existing = stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        stayPictureRepository.deleteById(id)
        deleteFile(existing.url)
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty")
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must be an image (got: $contentType)")
        }
    }

    private fun saveFile(file: MultipartFile, stayId: Int): String {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val filename = "${UUID.randomUUID()}.$ext"
        val dir = Path.of(uploadDir).toAbsolutePath().resolve("stays/$stayId")
        Files.createDirectories(dir)
        file.inputStream.use { Files.copy(it, dir.resolve(filename)) }
        return "/uploads/stays/$stayId/$filename"
    }

    private fun deleteFile(url: String) {
        try {
            val relativePath = url.removePrefix("/uploads/")
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().resolve(relativePath))
        } catch (_: Exception) {}
    }
}

private fun StayPicture.toResponse(): StayPictureResponse =
    StayPictureResponse(
        id = id,
        stayId = stayId,
        url = url,
        caption = caption,
        isPrimary = isPrimary,
        displayOrder = displayOrder
    )
