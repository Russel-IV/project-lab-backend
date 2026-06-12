package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.BookingRequest
import com.team1.project_lab_backend.dto.BookingResponse
import com.team1.project_lab_backend.dto.BookingStatusRequest
import com.team1.project_lab_backend.models.Booking
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.repositories.BookingRepository
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.util.orBadRequest
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireAllPositive
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

private val ACTIVE_STATUSES = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) {
    @Transactional(readOnly = true)
    fun getAllBookings(): List<BookingResponse> =
        bookingRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getBookingById(id: Int): BookingResponse {
        id.requirePositive()
        return bookingRepository.findById(id).orNotFound("booking not found").toResponse()
    }

    @Transactional
    fun createBooking(request: BookingRequest): BookingResponse {
        request.userId.requirePositive("userId")
        val user = userRepository.findById(request.userId).orBadRequest("userId not found")

        if (request.roomIds.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "roomIds must not be empty")
        }
        request.roomIds.requireAllPositive("roomIds")

        val today = LocalDate.now()
        if (request.checkInDate.isBefore(today)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate must not be in the past")
        }
        if (request.checkInDate.isAfter(today.plusMonths(6))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate must be within 6 months from today")
        }
        if (!request.checkOutDate.isAfter(request.checkInDate)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOutDate must be after checkInDate")
        }
        request.guestsCount.requirePositive("guestsCount")

        val rooms = roomRepository.findAllById(request.roomIds).toList()
        if (rooms.size != request.roomIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "roomIds contains unknown ids")
        }

        val distinctStayIds = rooms.map { it.stayId }.toSet()
        if (distinctStayIds.size > 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "all rooms must belong to the same stay")
        }

        val conflictingRooms = roomRepository.findConflictingRooms(
            request.roomIds,
            request.checkInDate,
            request.checkOutDate,
            ACTIVE_STATUSES
        )
        if (conflictingRooms.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "one or more rooms are not available for the requested dates"
            )
        }

        val totalCapacity = rooms.sumOf { it.sleeps }
        if (request.guestsCount > totalCapacity) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "guestsCount exceeds total capacity of requested rooms"
            )
        }

        val booking = Booking(
            id = 0,
            user = user,
            checkInDate = request.checkInDate,
            checkOutDate = request.checkOutDate,
            status = BookingStatus.PENDING,
            guestsCount = request.guestsCount,
            rooms = rooms.toMutableSet()
        )
        return bookingRepository.save(booking).toResponse()
    }

    @Transactional
    fun updateBookingStatus(id: Int, request: BookingStatusRequest): BookingResponse {
        id.requirePositive()
        val existing = bookingRepository.findById(id).orNotFound("booking not found")
        val updated = Booking(
            id = existing.id,
            user = existing.user,
            checkInDate = existing.checkInDate,
            checkOutDate = existing.checkOutDate,
            status = request.status,
            guestsCount = existing.guestsCount,
            createdAt = existing.createdAt,
            rooms = existing.rooms
        )
        return bookingRepository.save(updated).toResponse()
    }

    @Transactional
    fun deleteBooking(id: Int) {
        id.requirePositive()
        bookingRepository.requireExistsById(id, "booking not found")
        bookingRepository.deleteById(id)
    }
}

private fun Booking.toResponse(): BookingResponse =
    BookingResponse(
        id = id,
        userId = user.id,
        checkInDate = checkInDate,
        checkOutDate = checkOutDate,
        status = status,
        guestsCount = guestsCount,
        createdAt = createdAt,
        roomIds = rooms.map { it.id }.toSet()
    )
