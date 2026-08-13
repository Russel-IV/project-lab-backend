package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.inventory.services.PaymentTypeService
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
@RequestMapping("/internal/payment-types")
class PaymentTypeController(private val paymentTypeService: PaymentTypeService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<PaymentType> =
        if (ids != null) paymentTypeService.getAllById(ids) else paymentTypeService.getAllPaymentTypes()

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): PaymentType = paymentTypeService.getPaymentTypeById(id)

    @PostMapping
    fun create(
        @RequestBody request: PaymentTypeRequest,
    ): PaymentType = paymentTypeService.createPaymentType(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: PaymentTypeRequest,
    ): PaymentType = paymentTypeService.updatePaymentType(id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        paymentTypeService.deletePaymentType(id)
        return ResponseEntity.noContent().build()
    }
}
