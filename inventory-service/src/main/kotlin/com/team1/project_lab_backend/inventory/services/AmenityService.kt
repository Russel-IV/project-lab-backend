package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.repositories.AmenityRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmenityService(
    private val amenityRepository: AmenityRepository,
) {
    @Transactional(readOnly = true)
    fun getAllAmenities(): List<Amenity> = amenityRepository.findAll()

    @Transactional(readOnly = true)
    fun getAllById(ids: List<Int>): List<Amenity> = amenityRepository.findAllById(ids)

    @Transactional(readOnly = true)
    fun getAmenityById(id: Int): Amenity {
        id.requirePositive()
        return amenityRepository.findById(id).orNotFound("amenity not found")
    }

    @Transactional
    fun createAmenity(request: AmenityRequest): Amenity {
        request.name.requireNotBlank("name")
        return amenityRepository.save(Amenity(name = request.name, type = request.type))
    }

    @Transactional
    fun updateAmenity(
        id: Int,
        request: AmenityRequest,
    ): Amenity {
        id.requirePositive()
        request.name.requireNotBlank("name")
        amenityRepository.requireExistsById(id, "amenity not found")
        return amenityRepository.save(Amenity(id = id, name = request.name, type = request.type))
    }

    @Transactional
    fun deleteAmenity(id: Int) {
        id.requirePositive()
        amenityRepository.requireExistsById(id, "amenity not found")
        amenityRepository.deleteById(id)
    }
}
