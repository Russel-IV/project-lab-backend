package com.team1.project_lab_backend.inventory.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

/**
 * Resolves via Eureka to booking-service's internal REST API (docs/adr/0010). Booking
 * itself isn't extracted until Phase 6 — until then this name resolves to the Gateway,
 * which registers under it via EUREKA_APP_NAME (docker-compose.yml) since it still
 * hosts Booking's code locally. Phase 6 cuts the registration over to the real,
 * extracted booking-service with no change needed here.
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
