package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Amenity CRUD now lives in inventory-service,
 * reached via amenityFeignClient.
 */
@Service
class AmenityService(private val amenityFeignClient: AmenityFeignClient) {
    suspend fun getAllAmenities(): List<Amenity> = amenityFeignClient.list(ids = null)

    suspend fun getAmenityById(id: Int): Amenity =
        try {
            amenityFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }

    suspend fun createAmenity(request: AmenityRequest): Amenity =
        try {
            amenityFeignClient.create(request)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid amenity")
        }

    suspend fun updateAmenity(
        id: Int,
        request: AmenityRequest,
    ): Amenity =
        try {
            amenityFeignClient.update(id, request)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid amenity")
        }

    suspend fun deleteAmenity(id: Int) {
        try {
            amenityFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "amenity not found")
        }
    }
}
