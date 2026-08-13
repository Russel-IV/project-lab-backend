package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.BookingConfirmationEmailRequest
import com.team1.project_lab_backend.identity.services.EmailService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Internal-only (docs/adr/0005) — lets other services trigger identity-service-owned
 * email delivery without duplicating SMTP config/EmailService itself. booking-service's
 * EmailFeignClient is the first caller.
 */
@RestController
@RequestMapping("/internal/emails")
class EmailController(private val emailService: EmailService) {
    @PostMapping("/booking-confirmation")
    fun sendBookingConfirmation(
        @RequestBody request: BookingConfirmationEmailRequest,
    ): ResponseEntity<Void> {
        emailService.sendBookingConfirmationEmail(
            to = request.email,
            name = request.name,
            stayName = request.stayName,
            cityCountry = request.cityCountry,
            checkInDate = request.checkInDate,
            checkOutDate = request.checkOutDate,
            totalPrice = request.totalPrice,
            bookingId = request.bookingId,
        )
        return ResponseEntity.noContent().build()
    }
}
