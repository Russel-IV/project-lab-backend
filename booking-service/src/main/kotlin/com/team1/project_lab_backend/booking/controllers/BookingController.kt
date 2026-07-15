package com.team1.project_lab_backend.booking.controllers

import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.dto.CreateBookingRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.services.BookingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Internal-only API (docs/adr/0005) — the Gateway's BookingFeignClient is the only
 * caller. Not part of the public contract, so the shape here is whatever's convenient
 * for that one caller, not a versioned public REST API.
 */
@RestController
@RequestMapping("/internal/bookings")
class BookingController(private val bookingService: BookingService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(required = false) userId: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<Booking> = when {
        ids != null -> bookingService.findByIds(ids)
        userId != null -> bookingService.getBookingsByUser(userId, page, size)
        else -> bookingService.getAllBookings(page, size)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): Booking = bookingService.getBookingById(id)

    @GetMapping("/completed-for-stay")
    fun hasCompletedBookingForStay(@RequestParam userId: Int, @RequestParam stayId: Int): Boolean =
        bookingService.hasCompletedBookingForStay(userId, stayId)

    @PostMapping
    fun create(@RequestBody request: CreateBookingRequest): Booking = bookingService.createBooking(request)

    @PatchMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Int, @RequestBody request: BookingStatusRequest): Booking =
        bookingService.updateBookingStatus(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, @RequestParam requestingUserId: Int): ResponseEntity<Void> {
        bookingService.deleteBooking(id, requestingUserId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
