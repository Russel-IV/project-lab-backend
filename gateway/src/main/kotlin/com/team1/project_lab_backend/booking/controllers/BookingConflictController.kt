package com.team1.project_lab_backend.booking.controllers

import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

private val ACTIVE_STATUSES = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

/**
 * Internal-only API (docs/adr/0005, docs/adr/0010) consumed by inventory-service's
 * BookingFeignClient (search availability filtering, RoomService.getAvailableRooms).
 * Booking isn't extracted until Phase 6 — until then this endpoint lives on the
 * Gateway, which registers in Eureka under the "booking-service" name for exactly
 * this purpose (docker-compose.yml's EUREKA_APP_NAME override); the plan's Phase 6
 * cutover moves this controller (unchanged) to the real booking-service with no
 * change needed on inventory-service's side.
 */
@RestController
@RequestMapping("/internal/bookings")
class BookingConflictController(private val bookingRepository: BookingRepository) {

    @GetMapping("/conflicting-room-ids")
    fun conflictingRoomIds(
        @RequestParam(required = false) roomIds: List<Int>?,
        @RequestParam checkIn: LocalDate,
        @RequestParam checkOut: LocalDate,
    ): Set<Int> = bookingRepository.findConflictingRoomIds(roomIds, checkIn, checkOut, ACTIVE_STATUSES)
}
