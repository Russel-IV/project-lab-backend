package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.BookingRequest
import com.team1.project_lab_backend.dto.BookingResponse
import com.team1.project_lab_backend.dto.BookingStatusRequest
import com.team1.project_lab_backend.services.BookingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bookings")
class BookingController(
    private val bookingService: BookingService
) {
    @GetMapping
    fun getAllBookings(): ResponseEntity<List<BookingResponse>> =
        ResponseEntity.ok(bookingService.getAllBookings())

    @GetMapping("/{id}")
    fun getBookingById(@PathVariable id: Int): ResponseEntity<BookingResponse> =
        ResponseEntity.ok(bookingService.getBookingById(id))

    @PostMapping
    fun createBooking(@RequestBody request: BookingRequest): ResponseEntity<BookingResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request))

    @PatchMapping("/{id}/status")
    fun updateBookingStatus(
        @PathVariable id: Int,
        @RequestBody request: BookingStatusRequest
    ): ResponseEntity<BookingResponse> =
        ResponseEntity.ok(bookingService.updateBookingStatus(id, request))

    @DeleteMapping("/{id}")
    fun deleteBooking(@PathVariable id: Int): ResponseEntity<Unit> =
        bookingService.deleteBooking(id).let { ResponseEntity.noContent().build() }
}
