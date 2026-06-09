package com.team1.project_lab_backend.dto

import com.team1.project_lab_backend.models.BookingStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class BookingRequest(
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>
)

data class BookingResponse(
    val id: Int,
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val status: BookingStatus,
    val guestsCount: Int,
    val createdAt: LocalDateTime,
    val roomIds: Set<Int>
)

data class BookingStatusRequest(
    val status: BookingStatus
)
