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

/**
 * No longer a JPA @Entity — Booking is owned by booking-service now (docs/adr/0002,
 * docs/adr/0010, docs/adr/0011). This is a plain DTO: the GraphQL return type for
 * BookingResolver/BookingBatchResolver, and the JSON shape booking-service's internal
 * REST API serializes/deserializes.
 */
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
