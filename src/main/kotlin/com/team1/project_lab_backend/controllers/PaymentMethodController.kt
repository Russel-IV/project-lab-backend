package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.dto.PaymentMethodResponse
import com.team1.project_lab_backend.services.PaymentMethodService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payment-methods")
class PaymentMethodController(
    private val paymentMethodService: PaymentMethodService,
) {
    @GetMapping
    fun getPaymentMethods(): List<PaymentMethodResponse> {
        val currentUser = requireAuthenticated()
        return paymentMethodService.getPaymentMethods(currentUser.id)
    }

    @PostMapping
    fun createPaymentMethod(
        @RequestBody request: CreatePaymentMethodRequest,
    ): ResponseEntity<PaymentMethodResponse> {
        val currentUser = requireAuthenticated()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentMethodService.createPaymentMethod(currentUser.id, request))
    }
}
