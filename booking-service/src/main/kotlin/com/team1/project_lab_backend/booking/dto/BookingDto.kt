package com.team1.project_lab_backend.booking.dto

import com.team1.project_lab_backend.booking.models.BookingStatus
import java.time.LocalDate

/**
 * Internal-API request body for POST /internal/bookings. userId travels explicitly
 * (rather than being re-derived here) because it's always the Gateway's
 * JWT-authenticated caller's own id (docs/adr/0011) — this service has no way to
 * validate a JWT itself and doesn't need to: existence is implied by the Gateway
 * having accepted the request in the first place.
 */
data class CreateBookingRequest(
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>,
)

data class BookingStatusRequest(
    val status: BookingStatus,
)
