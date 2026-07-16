package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): PropertyBrand CRUD now lives in inventory-service,
 * reached via propertyBrandFeignClient.
 */
@Service
class PropertyBrandService(private val propertyBrandFeignClient: PropertyBrandFeignClient) {
    fun getAllPropertyBrands(): List<PropertyBrand> = propertyBrandFeignClient.list(ids = null)

    fun getPropertyBrandById(id: Int): PropertyBrand =
        try {
            propertyBrandFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }

    fun createPropertyBrand(request: PropertyBrandRequest): PropertyBrand =
        try {
            propertyBrandFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid property brand")
        }

    fun updatePropertyBrand(
        id: Int,
        request: PropertyBrandRequest,
    ): PropertyBrand =
        try {
            propertyBrandFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid property brand")
        }

    fun deletePropertyBrand(id: Int) {
        try {
            propertyBrandFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }
    }
}
