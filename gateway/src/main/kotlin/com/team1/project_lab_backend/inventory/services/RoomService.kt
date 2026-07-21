package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

/**
 * Orchestration shim (docs/adr/0005): Room CRUD and availability now live in
 * inventory-service, reached via roomFeignClient. Ownership (stay.hostId ==
 * requestingUserId) is now checked inside inventory-service itself, same pattern as
 * StayService.
 */
@Service
class RoomService(
    private val roomFeignClient: RoomFeignClient,
    private val stayService: StayService,
) {
    suspend fun getRoomsForStay(
        stayId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Room> =
        try {
            roomFeignClient.list(ids = null, stayId = stayId, stayIds = null, page = page, size = size)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }

    suspend fun getRoomById(id: Int): Room =
        try {
            roomFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        }

    /**
     * Cross-domain ownership check (room -> stay -> host), same purpose/rationale as
     * StayService.requireOwnedByHost — used by inventory.resolvers.RoomPictureResolver.
     */
    suspend fun requireOwnedByHost(
        id: Int,
        requestingUserId: Int,
    ): Room {
        val room = getRoomById(id)
        stayService.requireOwnedByHost(room.stayId, requestingUserId)
        return room
    }

    suspend fun getAvailableRooms(
        stayId: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int? = null,
    ): List<Room> =
        try {
            roomFeignClient.available(stayId, checkIn, checkOut, guests)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid availability request")
        }

    suspend fun createRoom(
        stayId: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        try {
            roomFeignClient.create(stayId, request, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid room")
        }

    suspend fun updateRoom(
        id: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        try {
            roomFeignClient.update(id, request, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid room")
        }

    suspend fun deleteRoom(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            roomFeignClient.delete(id, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
