package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

/**
 * Orchestration shim (docs/adr/0005): Room CRUD and availability now live in
 * inventory-service, reached via roomFeignClient. Ownership (stay.hostId ==
 * requestingUserId) is now checked inside inventory-service itself, same pattern as
 * StayService.
 */
@Service
class RoomService(private val roomFeignClient: RoomFeignClient) {
    fun getRoomsForStay(
        stayId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Room> =
        try {
            roomFeignClient.list(ids = null, stayId = stayId, stayIds = null, page = page, size = size)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }

    fun getRoomById(id: Int): Room =
        try {
            roomFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        }

    fun getAvailableRooms(
        stayId: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int? = null,
    ): List<Room> =
        try {
            roomFeignClient.available(stayId, checkIn, checkOut, guests)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid availability request")
        }

    fun createRoom(
        stayId: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        try {
            roomFeignClient.create(stayId, request, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid room")
        }

    fun updateRoom(
        id: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        try {
            roomFeignClient.update(id, request, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid room")
        }

    fun deleteRoom(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            roomFeignClient.delete(id, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
