package com.team1.project_lab_backend.booking.dto

import com.team1.project_lab_backend.booking.models.BookingStatus
import java.time.LocalDate

data class BookingRequest(
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>,
    val paymentIntentId: String,
)

data class BookingStatusRequest(
    val status: BookingStatus,
)

data class PaymentIntentRequest(
    val userId: Int,
    val roomIds: Set<Int>,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val idempotencyKey: String,
)
