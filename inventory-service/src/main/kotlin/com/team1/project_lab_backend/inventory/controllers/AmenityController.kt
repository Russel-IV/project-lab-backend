package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.services.AmenityService
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

/**
 * Internal-only API (docs/adr/0005) — the Gateway's AmenityFeignClient is the
 * only caller.
 */
@RestController
@RequestMapping("/internal/amenities")
class AmenityController(private val amenityService: AmenityService) {

    @GetMapping
    fun list(@RequestParam(required = false) ids: List<Int>?): List<Amenity> =
        if (ids != null) amenityService.getAllById(ids) else amenityService.getAllAmenities()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): Amenity = amenityService.getAmenityById(id)

    @PostMapping
    fun create(@RequestBody request: AmenityRequest): Amenity = amenityService.createAmenity(request)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: AmenityRequest): Amenity =
        amenityService.updateAmenity(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Void> {
        amenityService.deleteAmenity(id)
        return ResponseEntity.noContent().build()
    }
}
