package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): the actual Booking data and its own business
 * rules (date/capacity/conflict validation, total-price calculation, room existence
 * via inventory-service) now live in booking-service, reached via bookingFeignClient.
 * Unlike Phase 2/3's shims, there's no cross-domain validation left for the Gateway to
 * do here — booking-service can already reach inventory-service itself (docs/adr/0010),
 * and userId existence is never checked at all (implied by a valid JWT, docs/adr/0011).
 * This is purely FeignException -> ResponseStatusException translation.
 */
@Service
class BookingService(private val bookingFeignClient: BookingFeignClient) {

    fun getAllBookings(page: Int = 0, size: Int = 20): List<Booking> =
        bookingFeignClient.list(ids = null, userId = null, page = page, size = size)

    fun getBookingsByUser(userId: Int, page: Int = 0, size: Int = 20): List<Booking> =
        bookingFeignClient.list(ids = null, userId = userId, page = page, size = size)

    fun getBookingById(id: Int): Booking = try {
        bookingFeignClient.get(id)
    } catch (e: FeignException.NotFound) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
    }

    fun hasCompletedBookingForStay(userId: Int, stayId: Int): Boolean =
        bookingFeignClient.hasCompletedBookingForStay(userId, stayId)

    fun createBooking(request: BookingRequest): Booking = try {
        bookingFeignClient.create(
            CreateBookingRequest(
                userId = request.userId,
                checkInDate = request.checkInDate,
                checkOutDate = request.checkOutDate,
                guestsCount = request.guestsCount,
                roomIds = request.roomIds,
            ),
        )
    } catch (e: FeignException.BadRequest) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid booking")
    }

    fun updateBookingStatus(id: Int, request: BookingStatusRequest): Booking = try {
        bookingFeignClient.updateStatus(id, BookingStatusUpdateRequest(request.status))
    } catch (e: FeignException.NotFound) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
    }

    fun deleteBooking(id: Int, requestingUserId: Int) {
        try {
            bookingFeignClient.delete(id, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
