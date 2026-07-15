package com.team1.project_lab_backend.inventory.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

/**
 * Resolves via Eureka to the real booking-service's internal REST API (docs/adr/0010).
 * Through Phase 5 this name resolved to the Gateway (which registered under it via
 * EUREKA_APP_NAME while still hosting Booking's code locally); Phase 6 extracted
 * Booking into its own service and cut the registration over, with no change needed
 * to this client at all.
 */
@FeignClient(name = "booking-service")
interface BookingFeignClient {
    @GetMapping("/internal/bookings/conflicting-room-ids")
    fun getConflictingRoomIds(
        @RequestParam(required = false) roomIds: List<Int>?,
        @RequestParam checkIn: LocalDate,
        @RequestParam checkOut: LocalDate,
    ): Set<Int>
}
