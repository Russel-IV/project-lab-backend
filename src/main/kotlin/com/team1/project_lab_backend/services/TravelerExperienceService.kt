package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.dto.TravelerExperienceResponse
import com.team1.project_lab_backend.models.TravelerExperience
import com.team1.project_lab_backend.repositories.TravelerExperienceRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TravelerExperienceService(
    private val travelerExperienceRepository: TravelerExperienceRepository
) {
    @Transactional(readOnly = true)
    fun getAllTravelerExperiences(): List<TravelerExperienceResponse> =
        travelerExperienceRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getTravelerExperienceById(id: Int): TravelerExperienceResponse {
        id.requirePositive()
        return travelerExperienceRepository.findById(id).orNotFound("traveler experience not found").toResponse()
    }

    @Transactional
    fun createTravelerExperience(request: TravelerExperienceRequest): TravelerExperienceResponse {
        request.travelerExperienceType.requireNotBlank("travelerExperienceType")
        val travelerExperience = TravelerExperience(travelerExperienceType = request.travelerExperienceType)
        return travelerExperienceRepository.save(travelerExperience).toResponse()
    }

    @Transactional
    fun updateTravelerExperience(id: Int, request: TravelerExperienceRequest): TravelerExperienceResponse {
        id.requirePositive()
        request.travelerExperienceType.requireNotBlank("travelerExperienceType")
        travelerExperienceRepository.requireExistsById(id, "traveler experience not found")
        val travelerExperience = TravelerExperience(id = id, travelerExperienceType = request.travelerExperienceType)
        return travelerExperienceRepository.save(travelerExperience).toResponse()
    }

    @Transactional
    fun deleteTravelerExperience(id: Int) {
        id.requirePositive()
        travelerExperienceRepository.requireExistsById(id, "traveler experience not found")
        travelerExperienceRepository.deleteById(id)
    }
}

private fun TravelerExperience.toResponse(): TravelerExperienceResponse =
    TravelerExperienceResponse(id = id, travelerExperienceType = travelerExperienceType)
