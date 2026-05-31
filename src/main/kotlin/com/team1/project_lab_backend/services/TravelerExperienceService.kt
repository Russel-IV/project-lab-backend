package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.dto.TravelerExperienceResponse
import com.team1.project_lab_backend.models.TravelerExperience
import com.team1.project_lab_backend.repositories.TravelerExperienceRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TravelerExperienceService(
    private val travelerExperienceRepository: TravelerExperienceRepository
) {
    @Transactional(readOnly = true)
    fun getAllTravelerExperiences(): List<TravelerExperienceResponse> =
        travelerExperienceRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getTravelerExperienceById(id: Int): TravelerExperienceResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return travelerExperienceRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "traveler experience not found") }
    }

    @Transactional
    fun createTravelerExperience(request: TravelerExperienceRequest): TravelerExperienceResponse {
        if (request.travelerExperienceType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "travelerExperienceType must not be blank")
        }
        val travelerExperience = TravelerExperience(travelerExperienceType = request.travelerExperienceType)
        return travelerExperienceRepository.save(travelerExperience).toResponse()
    }

    @Transactional
    fun updateTravelerExperience(id: Int, request: TravelerExperienceRequest): TravelerExperienceResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.travelerExperienceType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "travelerExperienceType must not be blank")
        }
        if (!travelerExperienceRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "traveler experience not found")
        }
        val travelerExperience = TravelerExperience(id = id, travelerExperienceType = request.travelerExperienceType)
        return travelerExperienceRepository.save(travelerExperience).toResponse()
    }

    @Transactional
    fun deleteTravelerExperience(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!travelerExperienceRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "traveler experience not found")
        }
        travelerExperienceRepository.deleteById(id)
    }
}

private fun TravelerExperience.toResponse(): TravelerExperienceResponse =
    TravelerExperienceResponse(id = id, travelerExperienceType = travelerExperienceType)
