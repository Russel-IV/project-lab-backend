package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AmenityRequest
import com.team1.project_lab_backend.dto.AmenityResponse
import com.team1.project_lab_backend.models.Amenity
import com.team1.project_lab_backend.repositories.AmenityRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AmenityService(
    private val amenityRepository: AmenityRepository
) {
    @Transactional(readOnly = true)
    fun getAllAmenities(): List<AmenityResponse> =
        amenityRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getAmenityById(id: Int): AmenityResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return amenityRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found") }
    }

    @Transactional
    fun createAmenity(request: AmenityRequest): AmenityResponse {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        val amenity = Amenity(name = request.name, type = request.type)
        return amenityRepository.save(amenity).toResponse()
    }

    @Transactional
    fun updateAmenity(id: Int, request: AmenityRequest): AmenityResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (!amenityRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }
        val amenity = Amenity(id = id, name = request.name, type = request.type)
        return amenityRepository.save(amenity).toResponse()
    }

    @Transactional
    fun deleteAmenity(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!amenityRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }
        amenityRepository.deleteById(id)
    }
}

private fun Amenity.toResponse(): AmenityResponse =
    AmenityResponse(id = id, name = name, type = type)
