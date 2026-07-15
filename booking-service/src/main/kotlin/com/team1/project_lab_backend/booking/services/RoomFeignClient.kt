package com.team1.project_lab_backend.booking.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal

/**
 * Resolves via Eureka to inventory-service's internal REST API (docs/adr/0002,
 * docs/adr/0010). Only the bulk list-by-ids lookup is needed here — room
 * existence/stayId/price/capacity at booking-creation time, and resolving completed
 * bookings' room ids to stayIds for hasCompletedBookingForStay. This is a distinct
 * interface from the Gateway's own RoomFeignClient (no shared library between
 * modules), trimmed to only what this service actually calls.
 */
@FeignClient(name = "inventory-service", contextId = "bookingRoomFeignClient")
interface RoomFeignClient {

    @GetMapping("/internal/rooms")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(required = false) stayId: Int?,
        @RequestParam(required = false) stayIds: List<Int>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<RoomRef>
}

data class RoomRef(
    val id: Int,
    val stayId: Int,
    val price: BigDecimal,
    val sleeps: Int,
)
