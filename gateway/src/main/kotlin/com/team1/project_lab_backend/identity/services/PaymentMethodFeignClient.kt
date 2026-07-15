package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "identity-service", contextId = "paymentMethodFeignClient")
interface PaymentMethodFeignClient {

    @GetMapping("/internal/payment-methods")
    fun list(@RequestParam userId: Int): List<PaymentMethodResponse>

    @PostMapping("/internal/payment-methods")
    fun create(@RequestParam userId: Int, @RequestBody request: PaymentMethodCreateRequest): PaymentMethodResponse

    @PatchMapping("/internal/payment-methods/{id}/default")
    fun setDefault(@PathVariable id: Int, @RequestParam userId: Int)

    @DeleteMapping("/internal/payment-methods/{id}")
    fun delete(@PathVariable id: Int, @RequestParam userId: Int)
}

data class PaymentMethodCreateRequest(
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
)
