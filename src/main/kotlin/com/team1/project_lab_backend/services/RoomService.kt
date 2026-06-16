package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

private val ACTIVE_STATUSES = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository,
) {
    @Transactional(readOnly = true)
    fun getRoomsForStay(stayId: Int, page: Int = 0, size: Int = 20): List<Room> {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return roomRepository.findByStayId(stayId)
            .let { if (size > 0) it.drop(page * size).take(size) else it }
    }

    @Transactional(readOnly = true)
    fun getRoomById(id: Int): Room {
        id.requirePositive()
        return roomRepository.findById(id).orNotFound("room not found")
    }

    @Transactional
    fun createRoom(stayId: Int, request: RoomRequest): Room {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        validateRoomRequest(request)
        return roomRepository.save(
            Room(
                id = 0,
                stayId = stayId,
                name = request.name,
                price = request.price,
                sleeps = request.sleeps,
                bedroomAmount = request.bedroomAmount,
                bathrooms = request.bathrooms,
                size = request.size,
            ),
        )
    }

    @Transactional
    fun updateRoom(id: Int, request: RoomRequest): Room {
        id.requirePositive()
        val existing = roomRepository.findById(id).orNotFound("room not found")
        validateRoomRequest(request)
        return roomRepository.save(
            Room(
                id = id,
                stayId = existing.stayId,
                name = request.name,
                price = request.price,
                sleeps = request.sleeps,
                bedroomAmount = request.bedroomAmount,
                bathrooms = request.bathrooms,
                size = request.size,
            ),
        )
    }

    @Transactional
    fun deleteRoom(id: Int) {
        id.requirePositive()
        roomRepository.requireExistsById(id, "room not found")
        roomRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAvailableRooms(stayId: Int, checkIn: LocalDate, checkOut: LocalDate): List<Room> {
        stayId.requirePositive("stayId")
        if (!checkOut.isAfter(checkIn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn")
        }
        return roomRepository.findAvailableRooms(stayId, checkIn, checkOut, ACTIVE_STATUSES)
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
