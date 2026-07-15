package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): TravelerExperience CRUD now lives in inventory-service,
 * reached via travelerExperienceFeignClient.
 */
@Service
class TravelerExperienceService(private val travelerExperienceFeignClient: TravelerExperienceFeignClient) {

    fun getAllTravelerExperiences(): List<TravelerExperience> = travelerExperienceFeignClient.list(ids = null)

    fun createTravelerExperience(request: TravelerExperienceRequest): TravelerExperience =
        try {
            travelerExperienceFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid traveler experience")
        }

    fun updateTravelerExperience(id: Int, request: TravelerExperienceRequest): TravelerExperience =
        try {
            travelerExperienceFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "traveler experience not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid traveler experience")
        }

    fun deleteTravelerExperience(id: Int) {
        try {
            travelerExperienceFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "traveler experience not found")
        }
    }
}
