package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.dto.RoomResponse
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayRepository
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
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!stayRepository.existsById(stayId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
        return roomRepository.findByStayId(stayId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getRoomById(id: Int): RoomResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return roomRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "room not found") }
    }

    @Transactional
    fun createRoom(stayId: Int, request: RoomRequest): RoomResponse {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!stayRepository.existsById(stayId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
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
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        val existing = roomRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "room not found") }
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
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!roomRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "room not found")
        }
        roomRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAvailableRooms(stayId: Int, checkIn: LocalDate, checkOut: LocalDate): List<RoomResponse> {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!checkOut.isAfter(checkIn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn")
        }
        return roomRepository.findAvailableRooms(stayId, checkIn, checkOut, ACTIVE_STATUSES)
            .map { it.toResponse() }
    }

    private fun validateRoomRequest(request: RoomRequest) {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (request.price.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be >= 0")
        }
        if (request.sleeps <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sleeps must be > 0")
        }
        if (request.bedroomAmount < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bedroomAmount must be >= 0")
        }
        if (request.bathrooms.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bathrooms must be >= 0")
        }
        if (request.size != null && request.size.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be >= 0")
        }
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
