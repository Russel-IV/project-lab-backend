package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import com.team1.project_lab_backend.identity.services.PaymentMethodService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/payment-methods")
class PaymentMethodController(private val paymentMethodService: PaymentMethodService) {

    @GetMapping
    fun list(@RequestParam userId: Int): List<PaymentMethodResponse> = paymentMethodService.getPaymentMethods(userId)

    @PostMapping
    fun create(@RequestParam userId: Int, @RequestBody request: CreatePaymentMethodRequest): ResponseEntity<PaymentMethodResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(paymentMethodService.createPaymentMethod(userId, request))

    @PatchMapping("/{id}/default")
    fun setDefault(@PathVariable id: Int, @RequestParam userId: Int): ResponseEntity<Void> {
        paymentMethodService.setDefaultPaymentMethod(userId, id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, @RequestParam userId: Int): ResponseEntity<Void> {
        paymentMethodService.deletePaymentMethod(userId, id)
        return ResponseEntity.noContent().build()
    }
}
