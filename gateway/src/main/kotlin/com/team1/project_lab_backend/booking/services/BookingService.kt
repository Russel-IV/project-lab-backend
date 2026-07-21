package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.dto.PaymentIntentRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): the actual Booking data and its own business
 * rules (date/capacity/conflict validation, total-price calculation, room existence
 * via inventory-service) now live in booking-service, reached via bookingFeignClient.
 * Unlike Phase 2/3's shims, there's no cross-domain validation left for the Gateway to
 * do here — booking-service can already reach inventory-service itself (docs/adr/0010),
 * and userId existence is never checked at all (implied by a valid JWT, docs/adr/0011).
 * This is purely downstream-error -> ResponseStatusException translation.
 */
@Service
class BookingService(private val bookingFeignClient: BookingFeignClient) {
    suspend fun getAllBookings(
        page: Int = 0,
        size: Int = 20,
    ): List<Booking> = bookingFeignClient.list(ids = null, userId = null, page = page, size = size)

    suspend fun getBookingsByUser(
        userId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Booking> = bookingFeignClient.list(ids = null, userId = userId, page = page, size = size)

    suspend fun getBookingById(id: Int): Booking =
        try {
            bookingFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
        }

    suspend fun hasCompletedBookingForStay(
        userId: Int,
        stayId: Int,
    ): Boolean = bookingFeignClient.hasCompletedBookingForStay(userId, stayId)

    suspend fun createBooking(request: BookingRequest): Booking =
        try {
            bookingFeignClient.create(
                CreateBookingRequest(
                    userId = request.userId,
                    checkInDate = request.checkInDate,
                    checkOutDate = request.checkOutDate,
                    guestsCount = request.guestsCount,
                    roomIds = request.roomIds,
                    paymentIntentId = request.paymentIntentId,
                ),
            )
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid booking")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }

    suspend fun createPaymentIntent(request: PaymentIntentRequest): PaymentIntentResponse =
        try {
            bookingFeignClient.createPaymentIntent(
                CreatePaymentIntentRequest(
                    userId = request.userId,
                    roomIds = request.roomIds,
                    checkInDate = request.checkInDate,
                    checkOutDate = request.checkOutDate,
                    guestsCount = request.guestsCount,
                    idempotencyKey = request.idempotencyKey,
                ),
            )
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid payment intent")
        }

    suspend fun updateBookingStatus(
        id: Int,
        request: BookingStatusRequest,
    ): Booking =
        try {
            bookingFeignClient.updateStatus(id, BookingStatusUpdateRequest(request.status))
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
        }

    suspend fun deleteBooking(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            bookingFeignClient.delete(id, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
