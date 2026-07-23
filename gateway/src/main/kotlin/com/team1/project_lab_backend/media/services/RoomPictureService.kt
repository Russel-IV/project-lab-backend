package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.dto.RoomPictureResponse
import com.team1.project_lab_backend.media.models.RoomPicture
import com.team1.project_lab_backend.util.webClientErrorMessage
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): picture storage/validation and the
 * one-primary-per-owner invariant now live in media-service, reached via
 * mediaFeignClient. The room-ownership check media-service can't do itself (resolve
 * room -> stay -> host) now lives in inventory.services.RoomService.requireOwnedByHost,
 * called by inventory.resolvers.RoomPictureResolver/
 * inventory.controllers.RoomPictureController before delegating here — moved out of
 * this class to break a Spring Modulith cycle (inventory -> media -> inventory); see
 * ModularityTests.kt.
 */
@Service
class RoomPictureService(
    private val mediaFeignClient: MediaFeignClient,
) {
    suspend fun getPicturesForRoom(roomId: Int): List<RoomPictureResponse> {
        roomId.requirePositive("roomId")
        return mediaFeignClient.listForOwner("ROOM", roomId).map { it.toRoomPictureResponse() }
    }

    suspend fun getPicturesForRoomAsEntities(roomId: Int): List<RoomPicture> {
        roomId.requirePositive("roomId")
        return mediaFeignClient.listForOwner("ROOM", roomId).map { it.toRoomPicture() }
    }

    suspend fun getPicturesForRooms(roomIds: List<Int>): Map<Int, List<RoomPicture>> =
        mediaFeignClient.listForOwners("ROOM", roomIds).map { it.toRoomPicture() }.groupBy { it.roomId }

    suspend fun addPicture(
        roomId: Int,
        file: FilePart,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
    ): RoomPictureResponse {
        roomId.requirePositive("roomId")
        displayOrder.requireNonNegative("displayOrder")
        return try {
            mediaFeignClient.upload("ROOM", roomId, file, caption, isPrimary, displayOrder).toRoomPictureResponse()
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid picture")
        }
    }

    suspend fun updatePictureMetadata(
        roomId: Int,
        id: Int,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
    ): RoomPicture {
        roomId.requirePositive("roomId")
        id.requirePositive()
        displayOrder.requireNonNegative("displayOrder")
        return try {
            mediaFeignClient.update("ROOM", roomId, id, UpdateMediaRequest(caption, isPrimary, displayOrder)).toRoomPicture()
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid picture")
        }
    }

    suspend fun deletePicture(
        roomId: Int,
        id: Int,
    ) {
        roomId.requirePositive("roomId")
        id.requirePositive()
        try {
            mediaFeignClient.delete("ROOM", roomId, id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        }
    }

    private fun MediaResponse.toRoomPicture() =
        RoomPicture(
            id = id,
            roomId = ownerId,
            url = url,
            thumbnailUrl = thumbnailUrl,
            url1024 = url1024,
            url512 = url512,
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )

    private fun MediaResponse.toRoomPictureResponse() =
        RoomPictureResponse(
            id = id,
            roomId = ownerId,
            url = url,
            thumbnailUrl = thumbnailUrl,
            url1024 = url1024,
            url512 = url512,
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )
}
