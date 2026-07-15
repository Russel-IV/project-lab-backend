package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import com.team1.project_lab_backend.inventory.services.RoomFeignClient
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireAllPositive
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

private val ACTIVE_STATUSES = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val roomFeignClient: RoomFeignClient,
) {
    @Transactional(readOnly = true)
    fun getAllBookings(page: Int = 0, size: Int = 20): List<Booking> =
        bookingRepository.findAll(PageRequest.of(page, size)).content

    @Transactional(readOnly = true)
    fun getBookingsByUser(userId: Int, page: Int = 0, size: Int = 20): List<Booking> {
        userId.requirePositive("userId")
        return bookingRepository.findByUserId(
            userId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInDate")),
        )
    }

    @Transactional(readOnly = true)
    fun getBookingById(id: Int): Booking {
        id.requirePositive()
        return bookingRepository.findById(id).orNotFound("booking not found")
    }

    // Room (and its stayId) moved to inventory-service (docs/adr/0002, docs/adr/0010,
    // Phase 5) — "does this user have a COMPLETED booking for this stay" can no longer
    // be answered in a single local query. Fetch this user's COMPLETED-booking room
    // ids locally (still Booking's own data), then resolve those ids' stayIds via a
    // bulk Feign call and check for a match.
    @Transactional(readOnly = true)
    fun hasCompletedBookingForStay(userId: Int, stayId: Int): Boolean {
        stayId.requirePositive("stayId")
        val roomIds = bookingRepository.findRoomIdsForUserWithStatus(userId, BookingStatus.COMPLETED)
        if (roomIds.isEmpty()) return false
        return roomFeignClient.list(ids = roomIds.toList(), stayId = null, stayIds = null, page = 0, size = 0)
            .any { it.stayId == stayId }
    }

    // request.userId is always the JWT-authenticated caller's own id
    // (BookingResolver.createBooking) — existence is implied by a valid JWT, not
    // re-checked (docs/adr/0011, Phase 4; User itself no longer has a local
    // repository here to check against anyway).
    @Transactional
    fun createBooking(request: BookingRequest): Booking {
        request.userId.requirePositive("userId")

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

        // Room existence/stayId/price/capacity moved to inventory-service (docs/adr/0002,
        // Phase 5) — a bulk Feign call instead of a local repository lookup.
        val rooms = roomFeignClient.list(ids = request.roomIds.toList(), stayId = null, stayIds = null, page = 0, size = 0)
        if (rooms.size != request.roomIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "roomIds contains unknown ids")
        }
        if (rooms.map { it.stayId }.toSet().size > 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "all rooms must belong to the same stay")
        }

        // Conflict check stays entirely local: it only needs Booking's own data
        // (booking + booking_room), no Room attribute is involved (docs/adr/0010).
        val conflicting = bookingRepository.findConflictingRoomIds(
            request.roomIds.toList(),
            request.checkInDate,
            request.checkOutDate,
            ACTIVE_STATUSES,
        )
        if (conflicting.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "one or more rooms are not available for the requested dates",
            )
        }
        if (request.guestsCount > rooms.sumOf { it.sleeps }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "guestsCount exceeds total capacity of requested rooms",
            )
        }

        val nights = (request.checkOutDate.toEpochDay() - request.checkInDate.toEpochDay()).toBigDecimal()
        val totalPrice = rooms.fold(BigDecimal.ZERO) { acc, room -> acc + room.price } * nights

        return bookingRepository.save(
            Booking(
                id = 0,
                userId = request.userId,
                checkInDate = request.checkInDate,
                checkOutDate = request.checkOutDate,
                status = BookingStatus.PENDING,
                guestsCount = request.guestsCount,
                totalPrice = totalPrice,
                roomIds = request.roomIds.toMutableSet(),
            ),
        )
    }

    @Transactional
    fun updateBookingStatus(id: Int, request: BookingStatusRequest): Booking {
        id.requirePositive()
        val existing = bookingRepository.findById(id).orNotFound("booking not found")
        return bookingRepository.save(
            Booking(
                id = existing.id,
                userId = existing.userId,
                checkInDate = existing.checkInDate,
                checkOutDate = existing.checkOutDate,
                status = request.status,
                guestsCount = existing.guestsCount,
                createdAt = existing.createdAt,
                totalPrice = existing.totalPrice,
                roomIds = existing.roomIds,
            ),
        )
    }

    @Transactional
    fun deleteBooking(id: Int, requestingUserId: Int) {
        id.requirePositive()
        val booking = bookingRepository.findById(id).orNotFound("booking not found")
        if (booking.userId != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        bookingRepository.deleteById(id)
    }
}
