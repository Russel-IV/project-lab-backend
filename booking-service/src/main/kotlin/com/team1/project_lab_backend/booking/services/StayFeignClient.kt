package com.team1.project_lab_backend.booking.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Resolves via Eureka to inventory-service's internal REST API (docs/adr/0002,
 * docs/adr/0010). Only used to look up the stay name/city/country for the
 * booking-confirmation email — trimmed to those fields, distinct from the
 * Gateway's own StayFeignClient.
 */
@FeignClient(name = "inventory-service", contextId = "bookingStayFeignClient")
interface StayFeignClient {
    @GetMapping("/internal/stays/{id}")
    fun get(
        @PathVariable id: Int,
    ): StayRef
}

data class StayRef(
    val id: Int,
    val name: String,
    val address: StayAddressRef,
)

data class StayAddressRef(
    val city: String,
    val countryCode: String,
)
