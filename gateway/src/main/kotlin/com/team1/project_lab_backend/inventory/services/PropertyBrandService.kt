package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): PropertyBrand CRUD now lives in inventory-service,
 * reached via propertyBrandFeignClient.
 */
@Service
class PropertyBrandService(private val propertyBrandFeignClient: PropertyBrandFeignClient) {
    suspend fun getAllPropertyBrands(): List<PropertyBrand> = propertyBrandFeignClient.list(ids = null)

    suspend fun getPropertyBrandById(id: Int): PropertyBrand =
        try {
            propertyBrandFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }

    suspend fun createPropertyBrand(request: PropertyBrandRequest): PropertyBrand =
        try {
            propertyBrandFeignClient.create(request)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid property brand")
        }

    suspend fun updatePropertyBrand(
        id: Int,
        request: PropertyBrandRequest,
    ): PropertyBrand =
        try {
            propertyBrandFeignClient.update(id, request)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid property brand")
        }

    suspend fun deletePropertyBrand(id: Int) {
        try {
            propertyBrandFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }
    }
}
