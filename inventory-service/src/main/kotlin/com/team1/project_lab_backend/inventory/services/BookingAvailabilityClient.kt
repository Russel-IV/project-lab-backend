package com.team1.project_lab_backend.inventory.services

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BookingAvailabilityClient(private val bookingFeignClient: BookingFeignClient) {
    @CircuitBreaker(name = "bookingConflictCheck", fallbackMethod = "conflictCheckFallback")
    fun getConflictingRoomIds(
        roomIds: List<Int>?,
        checkIn: LocalDate,
        checkOut: LocalDate,
    ): Set<Int> = bookingFeignClient.getConflictingRoomIds(roomIds, checkIn, checkOut)

    @Suppress("UNUSED_PARAMETER")
    private fun conflictCheckFallback(
        roomIds: List<Int>?,
        checkIn: LocalDate,
        checkOut: LocalDate,
        ex: Throwable,
    ): Set<Int> = emptySet()
}
