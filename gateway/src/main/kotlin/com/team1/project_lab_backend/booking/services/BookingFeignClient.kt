package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

/**
 * Resolves via Eureka to booking-service's internal REST API (docs/adr/0002,
 * docs/adr/0010, docs/adr/0011). Mirrors BookingController/BookingConflictController
 * one-to-one — this interface and those controllers are two halves of one contract
 * that must be kept in sync by hand, since there's no shared library between the two
 * modules.
 */
@FeignClient(name = "booking-service", contextId = "bookingFeignClient")
interface BookingFeignClient {
    @GetMapping("/internal/bookings")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(required = false) userId: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<Booking>

    @GetMapping("/internal/bookings/{id}")
    fun get(
        @PathVariable id: Int,
    ): Booking

    @GetMapping("/internal/bookings/completed-for-stay")
    fun hasCompletedBookingForStay(
        @RequestParam userId: Int,
        @RequestParam stayId: Int,
    ): Boolean

    @PostMapping("/internal/bookings")
    fun create(
        @RequestBody request: CreateBookingRequest,
    ): Booking

    @PatchMapping("/internal/bookings/{id}/status")
    fun updateStatus(
        @PathVariable id: Int,
        @RequestBody request: BookingStatusUpdateRequest,
    ): Booking

    @DeleteMapping("/internal/bookings/{id}")
    fun delete(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
    )
}

data class CreateBookingRequest(
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>,
)

data class BookingStatusUpdateRequest(val status: BookingStatus)
