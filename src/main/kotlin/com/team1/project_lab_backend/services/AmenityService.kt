package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AmenityRequest
import com.team1.project_lab_backend.dto.AmenityResponse
import com.team1.project_lab_backend.models.Amenity
import com.team1.project_lab_backend.repositories.AmenityRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmenityService(
    private val amenityRepository: AmenityRepository
) {
    @Transactional(readOnly = true)
    fun getAllAmenities(): List<AmenityResponse> =
        amenityRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getAmenityById(id: Int): AmenityResponse {
        id.requirePositive()
        return amenityRepository.findById(id).orNotFound("amenity not found").toResponse()
    }

    @Transactional
    fun createAmenity(request: AmenityRequest): AmenityResponse {
        request.name.requireNotBlank("name")
        val amenity = Amenity(name = request.name, type = request.type)
        return amenityRepository.save(amenity).toResponse()
    }

    @Transactional
    fun updateAmenity(id: Int, request: AmenityRequest): AmenityResponse {
        id.requirePositive()
        request.name.requireNotBlank("name")
        amenityRepository.requireExistsById(id, "amenity not found")
        val amenity = Amenity(id = id, name = request.name, type = request.type)
        return amenityRepository.save(amenity).toResponse()
    }

    @Transactional
    fun deleteAmenity(id: Int) {
        id.requirePositive()
        amenityRepository.requireExistsById(id, "amenity not found")
        amenityRepository.deleteById(id)
    }
}

private fun Amenity.toResponse(): AmenityResponse =
    AmenityResponse(id = id, name = name, type = type)
