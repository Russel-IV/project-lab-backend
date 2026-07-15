package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "paymentTypeFeignClient")
interface PaymentTypeFeignClient {

    @GetMapping("/internal/payment-types")
    fun list(@RequestParam(required = false) ids: List<Int>?): List<PaymentType>

    @GetMapping("/internal/payment-types/{id}")
    fun get(@PathVariable id: Int): PaymentType

    @PostMapping("/internal/payment-types")
    fun create(@RequestBody request: PaymentTypeRequest): PaymentType

    @PatchMapping("/internal/payment-types/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: PaymentTypeRequest): PaymentType

    @DeleteMapping("/internal/payment-types/{id}")
    fun delete(@PathVariable id: Int)
}
