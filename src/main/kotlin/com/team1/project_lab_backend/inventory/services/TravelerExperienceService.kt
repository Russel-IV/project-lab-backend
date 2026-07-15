package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import com.team1.project_lab_backend.inventory.repositories.TravelerExperienceRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TravelerExperienceService(
    private val travelerExperienceRepository: TravelerExperienceRepository,
) {
    @Transactional(readOnly = true)
    fun getAllTravelerExperiences(): List<TravelerExperience> = travelerExperienceRepository.findAll()

    @Transactional(readOnly = true)
    fun getTravelerExperienceById(id: Int): TravelerExperience {
        id.requirePositive()
        return travelerExperienceRepository.findById(id).orNotFound("traveler experience not found")
    }

    @Transactional
    fun createTravelerExperience(request: TravelerExperienceRequest): TravelerExperience {
        request.travelerExperienceType.requireNotBlank("travelerExperienceType")
        return travelerExperienceRepository.save(TravelerExperience(travelerExperienceType = request.travelerExperienceType))
    }

    @Transactional
    fun updateTravelerExperience(
        id: Int,
        request: TravelerExperienceRequest,
    ): TravelerExperience {
        id.requirePositive()
        request.travelerExperienceType.requireNotBlank("travelerExperienceType")
        travelerExperienceRepository.requireExistsById(id, "traveler experience not found")
        return travelerExperienceRepository.save(
            TravelerExperience(id = id, travelerExperienceType = request.travelerExperienceType),
        )
    }

    @Transactional
    fun deleteTravelerExperience(id: Int) {
        id.requirePositive()
        travelerExperienceRepository.requireExistsById(id, "traveler experience not found")
        travelerExperienceRepository.deleteById(id)
    }
}
