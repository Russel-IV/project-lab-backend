package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import com.team1.project_lab_backend.identity.services.PaymentMethodService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
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
    suspend fun getPaymentMethods(): List<PaymentMethodResponse> {
        val currentUser = requireAuthenticated()
        return paymentMethodService.getPaymentMethods(currentUser.id)
    }

    @PostMapping
    suspend fun createPaymentMethod(
        @RequestBody request: CreatePaymentMethodRequest,
    ): ResponseEntity<PaymentMethodResponse> {
        val currentUser = requireAuthenticated()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentMethodService.createPaymentMethod(currentUser.id, request))
    }

    @PatchMapping("/{id}/default")
    suspend fun setDefaultPaymentMethod(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        val currentUser = requireAuthenticated()
        paymentMethodService.setDefaultPaymentMethod(currentUser.id, id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    suspend fun deletePaymentMethod(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        val currentUser = requireAuthenticated()
        paymentMethodService.deletePaymentMethod(currentUser.id, id)
        return ResponseEntity.noContent().build()
    }
}
