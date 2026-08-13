package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import com.team1.project_lab_backend.inventory.services.TravelerExperienceService
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
@RequestMapping("/internal/traveler-experiences")
class TravelerExperienceController(private val travelerExperienceService: TravelerExperienceService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<TravelerExperience> =
        if (ids != null) travelerExperienceService.getAllById(ids) else travelerExperienceService.getAllTravelerExperiences()

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): TravelerExperience = travelerExperienceService.getTravelerExperienceById(id)

    @PostMapping
    fun create(
        @RequestBody request: TravelerExperienceRequest,
    ): TravelerExperience = travelerExperienceService.createTravelerExperience(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: TravelerExperienceRequest,
    ): TravelerExperience = travelerExperienceService.updateTravelerExperience(id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        travelerExperienceService.deleteTravelerExperience(id)
        return ResponseEntity.noContent().build()
    }
}
