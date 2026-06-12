package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.dto.RoomResponse
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

private val ACTIVE_STATUSES = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository
) {
    @Transactional(readOnly = true)
    fun getRoomsForStay(stayId: Int): List<RoomResponse> {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return roomRepository.findByStayId(stayId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getRoomById(id: Int): RoomResponse {
        id.requirePositive()
        return roomRepository.findById(id).orNotFound("room not found").toResponse()
    }

    @Transactional
    fun createRoom(stayId: Int, request: RoomRequest): RoomResponse {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        validateRoomRequest(request)
        val room = Room(
            id = 0,
            stayId = stayId,
            name = request.name,
            price = request.price,
            sleeps = request.sleeps,
            bedroomAmount = request.bedroomAmount,
            bathrooms = request.bathrooms,
            size = request.size
        )
        return roomRepository.save(room).toResponse()
    }

    @Transactional
    fun updateRoom(id: Int, request: RoomRequest): RoomResponse {
        id.requirePositive()
        val existing = roomRepository.findById(id).orNotFound("room not found")
        validateRoomRequest(request)
        val room = Room(
            id = id,
            stayId = existing.stayId,
            name = request.name,
            price = request.price,
            sleeps = request.sleeps,
            bedroomAmount = request.bedroomAmount,
            bathrooms = request.bathrooms,
            size = request.size
        )
        return roomRepository.save(room).toResponse()
    }

    @Transactional
    fun deleteRoom(id: Int) {
        id.requirePositive()
        roomRepository.requireExistsById(id, "room not found")
        roomRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAvailableRooms(stayId: Int, checkIn: LocalDate, checkOut: LocalDate): List<RoomResponse> {
        stayId.requirePositive("stayId")
        if (!checkOut.isAfter(checkIn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn")
        }
        return roomRepository.findAvailableRooms(stayId, checkIn, checkOut, ACTIVE_STATUSES)
            .map { it.toResponse() }
    }

    private fun validateRoomRequest(request: RoomRequest) {
        request.name.requireNotBlank("name")
        request.price.requireNonNegative("price")
        request.sleeps.requirePositive("sleeps")
        request.bedroomAmount.requireNonNegative("bedroomAmount")
        request.bathrooms.requireNonNegative("bathrooms")
        request.size?.requireNonNegative("size")
    }
}

private fun Room.toResponse(): RoomResponse =
    RoomResponse(
        id = id,
        stayId = stayId,
        name = name,
        price = price,
        sleeps = sleeps,
        bedroomAmount = bedroomAmount,
        bathrooms = bathrooms,
        size = size
    )
