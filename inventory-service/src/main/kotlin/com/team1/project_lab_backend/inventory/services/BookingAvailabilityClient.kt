package com.team1.project_lab_backend.inventory.services

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Wraps BookingFeignClient in a Resilience4j circuit breaker (docs/adr/0010): search
 * and availability must never fail outright just because booking-service is slow or
 * down. On any failure (timeout — see feign.client.config.booking-service.* in
 * application.properties — or open circuit) the fallback returns an empty conflict
 * set, i.e. "skip the filter" — StayService/RoomService then treat every room as
 * available rather than surfacing an error to the caller.
 */
@Component
class BookingAvailabilityClient(private val bookingFeignClient: BookingFeignClient) {

    @CircuitBreaker(name = "bookingConflictCheck", fallbackMethod = "conflictCheckFallback")
    fun getConflictingRoomIds(roomIds: List<Int>?, checkIn: LocalDate, checkOut: LocalDate): Set<Int> =
        bookingFeignClient.getConflictingRoomIds(roomIds, checkIn, checkOut)

    @Suppress("UNUSED_PARAMETER")
    private fun conflictCheckFallback(roomIds: List<Int>?, checkIn: LocalDate, checkOut: LocalDate, ex: Throwable): Set<Int> =
        emptySet()
}
