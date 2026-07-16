package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "travelerExperienceFeignClient")
interface TravelerExperienceFeignClient {
    @GetMapping("/internal/traveler-experiences")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<TravelerExperience>

    @GetMapping("/internal/traveler-experiences/{id}")
    fun get(
        @PathVariable id: Int,
    ): TravelerExperience

    @PostMapping("/internal/traveler-experiences")
    fun create(
        @RequestBody request: TravelerExperienceRequest,
    ): TravelerExperience

    @PatchMapping("/internal/traveler-experiences/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: TravelerExperienceRequest,
    ): TravelerExperience

    @DeleteMapping("/internal/traveler-experiences/{id}")
    fun delete(
        @PathVariable id: Int,
    )
}
