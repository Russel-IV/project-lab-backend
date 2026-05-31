package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.PropertyBrandRequest
import com.team1.project_lab_backend.dto.PropertyBrandResponse
import com.team1.project_lab_backend.services.PropertyBrandService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/property-brands")
class PropertyBrandController(
    private val propertyBrandService: PropertyBrandService
) {
    @GetMapping
    fun getAllPropertyBrands(): ResponseEntity<List<PropertyBrandResponse>> =
        ResponseEntity.ok(propertyBrandService.getAllPropertyBrands())

    @GetMapping("/{id}")
    fun getPropertyBrandById(@PathVariable id: Int): ResponseEntity<PropertyBrandResponse> =
        ResponseEntity.ok(propertyBrandService.getPropertyBrandById(id))

    @PostMapping
    fun createPropertyBrand(@RequestBody propertyBrand: PropertyBrandRequest): ResponseEntity<PropertyBrandResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(propertyBrandService.createPropertyBrand(propertyBrand))

    @PutMapping("/{id}")
    fun updatePropertyBrand(
        @PathVariable id: Int,
        @RequestBody propertyBrand: PropertyBrandRequest
    ): ResponseEntity<PropertyBrandResponse> =
        ResponseEntity.ok(propertyBrandService.updatePropertyBrand(id, propertyBrand))

    @DeleteMapping("/{id}")
    fun deletePropertyBrand(@PathVariable id: Int): ResponseEntity<Unit> =
        propertyBrandService.deletePropertyBrand(id).let { ResponseEntity.noContent().build() }
}
