package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.dto.TravelerExperienceResponse
import com.team1.project_lab_backend.services.TravelerExperienceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/traveler-experiences")
class TravelerExperienceController(
    private val travelerExperienceService: TravelerExperienceService
) {
    @GetMapping
    fun getAllTravelerExperiences(): ResponseEntity<List<TravelerExperienceResponse>> =
        ResponseEntity.ok(travelerExperienceService.getAllTravelerExperiences())

    @GetMapping("/{id}")
    fun getTravelerExperienceById(@PathVariable id: Int): ResponseEntity<TravelerExperienceResponse> =
        ResponseEntity.ok(travelerExperienceService.getTravelerExperienceById(id))

    @PostMapping
    fun createTravelerExperience(
        @RequestBody travelerExperience: TravelerExperienceRequest
    ): ResponseEntity<TravelerExperienceResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(travelerExperienceService.createTravelerExperience(travelerExperience))

    @PutMapping("/{id}")
    fun updateTravelerExperience(
        @PathVariable id: Int,
        @RequestBody travelerExperience: TravelerExperienceRequest
    ): ResponseEntity<TravelerExperienceResponse> =
        ResponseEntity.ok(travelerExperienceService.updateTravelerExperience(id, travelerExperience))

    @DeleteMapping("/{id}")
    fun deleteTravelerExperience(@PathVariable id: Int): ResponseEntity<Unit> =
        travelerExperienceService.deleteTravelerExperience(id).let { ResponseEntity.noContent().build() }
}
