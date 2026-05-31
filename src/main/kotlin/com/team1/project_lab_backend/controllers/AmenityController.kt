package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.AmenityRequest
import com.team1.project_lab_backend.dto.AmenityResponse
import com.team1.project_lab_backend.services.AmenityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/amenities")
class AmenityController(
    private val amenityService: AmenityService
) {
    @GetMapping
    fun getAllAmenities(): ResponseEntity<List<AmenityResponse>> =
        ResponseEntity.ok(amenityService.getAllAmenities())

    @GetMapping("/{id}")
    fun getAmenityById(@PathVariable id: Int): ResponseEntity<AmenityResponse> =
        ResponseEntity.ok(amenityService.getAmenityById(id))

    @PostMapping
    fun createAmenity(@RequestBody amenity: AmenityRequest): ResponseEntity<AmenityResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(amenityService.createAmenity(amenity))

    @PutMapping("/{id}")
    fun updateAmenity(@PathVariable id: Int, @RequestBody amenity: AmenityRequest): ResponseEntity<AmenityResponse> =
        ResponseEntity.ok(amenityService.updateAmenity(id, amenity))

    @DeleteMapping("/{id}")
    fun deleteAmenity(@PathVariable id: Int): ResponseEntity<Unit> =
        amenityService.deleteAmenity(id).let { ResponseEntity.noContent().build() }
}
