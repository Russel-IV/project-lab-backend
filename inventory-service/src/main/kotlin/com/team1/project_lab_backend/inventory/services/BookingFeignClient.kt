package com.team1.project_lab_backend.inventory.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@FeignClient(name = "booking-service")
interface BookingFeignClient {
    @GetMapping("/internal/bookings/conflicting-room-ids")
    fun getConflictingRoomIds(
        @RequestParam(required = false) roomIds: List<Int>?,
        @RequestParam checkIn: LocalDate,
        @RequestParam checkOut: LocalDate,
    ): Set<Int>
}
