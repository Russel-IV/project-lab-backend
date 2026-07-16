package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Amenity CRUD now lives in inventory-service,
 * reached via amenityFeignClient.
 */
@Service
class AmenityService(private val amenityFeignClient: AmenityFeignClient) {
    fun getAllAmenities(): List<Amenity> = amenityFeignClient.list(ids = null)

    fun getAmenityById(id: Int): Amenity =
        try {
            amenityFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }

    fun createAmenity(request: AmenityRequest): Amenity =
        try {
            amenityFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid amenity")
        }

    fun updateAmenity(
        id: Int,
        request: AmenityRequest,
    ): Amenity =
        try {
            amenityFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid amenity")
        }

    fun deleteAmenity(id: Int) {
        try {
            amenityFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }
    }
}
