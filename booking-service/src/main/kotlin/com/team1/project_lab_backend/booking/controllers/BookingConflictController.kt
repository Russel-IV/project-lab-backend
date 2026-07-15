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
 * Through Phase 5 this endpoint lived on the Gateway (registered in Eureka under
 * "booking-service" via EUREKA_APP_NAME) since Booking hadn't been extracted yet;
 * Phase 6 moves it here unchanged and cuts the real Eureka registration over —
 * inventory-service's Feign client needed no code change either way.
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
