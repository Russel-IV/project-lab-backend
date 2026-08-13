package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.inventory.services.PropertyBrandService
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
@RequestMapping("/internal/property-brands")
class PropertyBrandController(private val propertyBrandService: PropertyBrandService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<PropertyBrand> =
        if (ids != null) propertyBrandService.getAllById(ids) else propertyBrandService.getAllPropertyBrands()

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): PropertyBrand = propertyBrandService.getPropertyBrandById(id)

    @PostMapping
    fun create(
        @RequestBody request: PropertyBrandRequest,
    ): PropertyBrand = propertyBrandService.createPropertyBrand(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: PropertyBrandRequest,
    ): PropertyBrand = propertyBrandService.updatePropertyBrand(id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        propertyBrandService.deletePropertyBrand(id)
        return ResponseEntity.noContent().build()
    }
}
