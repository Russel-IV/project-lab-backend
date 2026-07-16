package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "propertyBrandFeignClient")
interface PropertyBrandFeignClient {
    @GetMapping("/internal/property-brands")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<PropertyBrand>

    @GetMapping("/internal/property-brands/{id}")
    fun get(
        @PathVariable id: Int,
    ): PropertyBrand

    @PostMapping("/internal/property-brands")
    fun create(
        @RequestBody request: PropertyBrandRequest,
    ): PropertyBrand

    @PatchMapping("/internal/property-brands/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: PropertyBrandRequest,
    ): PropertyBrand

    @DeleteMapping("/internal/property-brands/{id}")
    fun delete(
        @PathVariable id: Int,
    )
}
