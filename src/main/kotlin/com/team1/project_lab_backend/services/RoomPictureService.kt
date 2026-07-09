package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomPictureResponse
import com.team1.project_lab_backend.models.RoomPicture
import com.team1.project_lab_backend.repositories.RoomPictureRepository
import com.team1.project_lab_backend.repositories.RoomRepository
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
class RoomPictureService(
    private val roomPictureRepository: RoomPictureRepository,
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository,
    private val storageService: StorageService,
) {
    @Transactional(readOnly = true)
    fun getPicturesForRoom(roomId: Int): List<RoomPictureResponse> {
        roomId.requirePositive("roomId")
        return roomPictureRepository.findByRoomId(roomId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getPicturesForRoomAsEntities(roomId: Int): List<RoomPicture> {
        roomId.requirePositive("roomId")
        return roomPictureRepository.findByRoomId(roomId)
    }

    fun resolveUrl(roomPicture: RoomPicture): String = storageService.toUrl(roomPicture.url)

    @Transactional
    fun addPicture(
        roomId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): RoomPictureResponse {
        roomId.requirePositive("roomId")
        val room = roomRepository.findById(roomId).orNotFound("room not found")
        val stay = stayRepository.findById(room.stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        displayOrder.requireNonNegative("displayOrder")
        validateImageFile(file)
        if (isPrimary && roomPictureRepository.existsByRoomIdAndIsPrimary(roomId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this room")
        }
        val key = storageService.save(file, "rooms/$roomId")
        val picture = RoomPicture(
            id = 0, roomId = roomId, url = key,
            caption = caption, isPrimary = isPrimary, displayOrder = displayOrder,
        )
        try {
            return roomPictureRepository.save(picture).toResponse()
        } catch (e: Exception) {
            runCatching { storageService.delete(key) }
            throw e
        }
    }

    @Transactional
    fun updatePictureMetadata(
        roomId: Int,
        id: Int,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): RoomPicture {
        roomId.requirePositive("roomId")
        id.requirePositive()
        displayOrder.requireNonNegative("displayOrder")
        val room = roomRepository.findById(roomId).orNotFound("room not found")
        val stay = stayRepository.findById(room.stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        val existing = roomPictureRepository.findByRoomIdAndId(roomId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        if (isPrimary && !existing.isPrimary && roomPictureRepository.existsByRoomIdAndIsPrimary(roomId, true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "a primary picture already exists for this room")
        }
        return roomPictureRepository.save(
            RoomPicture(
                id = id,
                roomId = roomId,
                url = existing.url,
                caption = caption,
                isPrimary = isPrimary,
                displayOrder = displayOrder,
            ),
        )
    }

    @Transactional
    fun deletePicture(roomId: Int, id: Int, requestingUserId: Int) {
        roomId.requirePositive("roomId")
        id.requirePositive()
        val room = roomRepository.findById(roomId).orNotFound("room not found")
        val stay = stayRepository.findById(room.stayId).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        val existing = roomPictureRepository.findByRoomIdAndId(roomId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        roomPictureRepository.deleteById(id)
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

    private fun RoomPicture.toResponse(): RoomPictureResponse =
        RoomPictureResponse(
            id = id,
            roomId = roomId,
            url = storageService.toUrl(url),
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "avif")
    }
}
