package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.PaymentTypeRequest
import com.team1.project_lab_backend.dto.PaymentTypeResponse
import com.team1.project_lab_backend.services.PaymentTypeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payment-types")
class PaymentTypeController(
    private val paymentTypeService: PaymentTypeService
) {
    @GetMapping
    fun getAllPaymentTypes(): ResponseEntity<List<PaymentTypeResponse>> =
        ResponseEntity.ok(paymentTypeService.getAllPaymentTypes())

    @GetMapping("/{id}")
    fun getPaymentTypeById(@PathVariable id: Int): ResponseEntity<PaymentTypeResponse> =
        ResponseEntity.ok(paymentTypeService.getPaymentTypeById(id))

    @PostMapping
    fun createPaymentType(@RequestBody paymentType: PaymentTypeRequest): ResponseEntity<PaymentTypeResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(paymentTypeService.createPaymentType(paymentType))

    @PutMapping("/{id}")
    fun updatePaymentType(
        @PathVariable id: Int,
        @RequestBody paymentType: PaymentTypeRequest
    ): ResponseEntity<PaymentTypeResponse> =
        ResponseEntity.ok(paymentTypeService.updatePaymentType(id, paymentType))

    @DeleteMapping("/{id}")
    fun deletePaymentType(@PathVariable id: Int): ResponseEntity<Unit> =
        paymentTypeService.deletePaymentType(id).let { ResponseEntity.noContent().build() }
}
