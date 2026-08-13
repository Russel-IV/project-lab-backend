package com.team1.project_lab_backend.booking.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Resolves via Eureka to identity-service's internal REST API — identity-service owns
 * SMTP config and EmailService (docs/adr/0002), so triggering an email from
 * booking-service means calling its internal endpoint rather than duplicating mail
 * wiring here.
 */
@FeignClient(name = "identity-service", contextId = "bookingEmailFeignClient")
interface EmailFeignClient {
    @PostMapping("/internal/emails/booking-confirmation")
    fun sendBookingConfirmation(
        @RequestBody request: BookingConfirmationEmailRequest,
    )
}

data class BookingConfirmationEmailRequest(
    val email: String,
    val name: String,
    val stayName: String,
    val cityCountry: String,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val totalPrice: BigDecimal,
    val bookingId: Int,
)
