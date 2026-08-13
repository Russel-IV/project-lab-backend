package com.team1.project_lab_backend.booking.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Resolves via Eureka to identity-service's internal REST API (docs/adr/0002,
 * docs/adr/0010). Only used to look up the booking's owner name/email for the
 * booking-confirmation email — trimmed to those two fields, distinct from the
 * Gateway's own UserFeignClient.
 */
@FeignClient(name = "identity-service", contextId = "bookingUserFeignClient")
interface UserFeignClient {
    @GetMapping("/internal/users/{id}")
    fun get(
        @PathVariable id: Int,
    ): UserRef
}

data class UserRef(
    val id: Int,
    val name: String,
    val email: String?,
)
