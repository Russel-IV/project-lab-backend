package com.team1.project_lab_backend.booking.models

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}

data class Booking(
    val id: Int,
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val status: BookingStatus,
    val guestsCount: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val roomIds: Set<Int> = emptySet(),
)
