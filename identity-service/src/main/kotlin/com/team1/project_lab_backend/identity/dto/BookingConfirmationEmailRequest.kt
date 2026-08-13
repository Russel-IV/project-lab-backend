package com.team1.project_lab_backend.identity.dto

import java.math.BigDecimal
import java.time.LocalDate

data class BookingConfirmationEmailRequest(
    val email: String,
    val name: String,
    val stayName: String,
    val cityCountry: String,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val totalPrice: BigDecimal,
    val bookingId: Int,
)
