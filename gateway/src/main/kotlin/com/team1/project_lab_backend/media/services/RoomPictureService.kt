package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.inventory.repositories.RoomRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.media.dto.RoomPictureResponse
import com.team1.project_lab_backend.media.models.RoomPicture
import com.team1.project_lab_backend.util.feignErrorMessage
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requirePositive
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): picture storage/validation and the
 * one-primary-per-owner invariant now live in media-service, reached via
 * mediaFeignClient. What's still here is the ownership check media-service
 * can't do itself (resolve room -> stay -> host) — until Inventory is
 * extracted (Phase 5), at which point this becomes a Feign call too.
 */
@Service
class RoomPictureService(
    private val mediaFeignClient: MediaFeignClient,
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository,
) {
    fun getPicturesForRoom(roomId: Int): List<RoomPictureResponse> {
        roomId.requirePositive("roomId")
        return mediaFeignClient.listForOwner("ROOM", roomId).map { it.toRoomPictureResponse() }
    }

    fun getPicturesForRoomAsEntities(roomId: Int): List<RoomPicture> {
        roomId.requirePositive("roomId")
        return mediaFeignClient.listForOwner("ROOM", roomId).map { it.toRoomPicture() }
    }

    fun getPicturesForRooms(roomIds: List<Int>): Map<Int, List<RoomPicture>> =
        mediaFeignClient.listForOwners("ROOM", roomIds).map { it.toRoomPicture() }.groupBy { it.roomId }

    fun addPicture(
        roomId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): RoomPictureResponse {
        roomId.requirePositive("roomId")
        requireOwnedByRoomHost(roomId, requestingUserId)
        displayOrder.requireNonNegative("displayOrder")
        return try {
            mediaFeignClient.upload("ROOM", roomId, file, caption, isPrimary, displayOrder).toRoomPictureResponse()
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid picture")
        }
    }

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
        requireOwnedByRoomHost(roomId, requestingUserId)
        return try {
            mediaFeignClient.update("ROOM", roomId, id, UpdateMediaRequest(caption, isPrimary, displayOrder)).toRoomPicture()
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid picture")
        }
    }

    fun deletePicture(roomId: Int, id: Int, requestingUserId: Int) {
        roomId.requirePositive("roomId")
        id.requirePositive()
        requireOwnedByRoomHost(roomId, requestingUserId)
        try {
            mediaFeignClient.delete("ROOM", roomId, id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        }
    }

    private fun requireOwnedByRoomHost(roomId: Int, requestingUserId: Int) {
        val room = roomRepository.findById(roomId).orNotFound("room not found")
        val stay = stayRepository.findById(room.stayId).orNotFound("stay not found")
        if (stay.hostId != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
    }

    private fun MediaResponse.toRoomPicture() =
        RoomPicture(id = id, roomId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)

    private fun MediaResponse.toRoomPictureResponse() =
        RoomPictureResponse(id = id, roomId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)
}
