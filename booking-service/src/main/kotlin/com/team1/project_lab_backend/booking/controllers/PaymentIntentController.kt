package com.team1.project_lab_backend.booking.controllers

import com.team1.project_lab_backend.booking.dto.CreatePaymentIntentRequest
import com.team1.project_lab_backend.booking.dto.PaymentIntentResponse
import com.team1.project_lab_backend.booking.services.PaymentIntentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Internal-only API (docs/adr/0005) — the Gateway's BookingFeignClient is the only
 * caller. Not part of the public contract, same shape convention as BookingController.
 */
@RestController
@RequestMapping("/internal/payment-intents")
class PaymentIntentController(private val paymentIntentService: PaymentIntentService) {
    @PostMapping
    fun create(
        @RequestBody request: CreatePaymentIntentRequest,
    ): ResponseEntity<PaymentIntentResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(paymentIntentService.createPaymentIntent(request))
}
