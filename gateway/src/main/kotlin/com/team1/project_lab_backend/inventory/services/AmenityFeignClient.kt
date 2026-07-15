package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "amenityFeignClient")
interface AmenityFeignClient {

    @GetMapping("/internal/amenities")
    fun list(@RequestParam(required = false) ids: List<Int>?): List<Amenity>

    @GetMapping("/internal/amenities/{id}")
    fun get(@PathVariable id: Int): Amenity

    @PostMapping("/internal/amenities")
    fun create(@RequestBody request: AmenityRequest): Amenity

    @PatchMapping("/internal/amenities/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: AmenityRequest): Amenity

    @DeleteMapping("/internal/amenities/{id}")
    fun delete(@PathVariable id: Int)
}
