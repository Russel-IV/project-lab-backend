package com.team1.project_lab_backend.booking.dto

import java.time.LocalDate

/**
 * Internal-API request body for POST /internal/payment-intents. userId travels
 * explicitly for the same reason as CreateBookingRequest.userId — it's always the
 * Gateway's JWT-authenticated caller's own id.
 */
data class CreatePaymentIntentRequest(
    val userId: Int,
    val roomIds: Set<Int>,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val idempotencyKey: String,
)

data class PaymentIntentResponse(
    val paymentIntentId: String,
    val clientSecret: String,
    val amount: Int,
    val currency: String,
)
