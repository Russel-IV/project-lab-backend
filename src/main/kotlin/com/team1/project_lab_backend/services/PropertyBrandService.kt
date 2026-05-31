package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.PropertyBrandRequest
import com.team1.project_lab_backend.dto.PropertyBrandResponse
import com.team1.project_lab_backend.models.PropertyBrand
import com.team1.project_lab_backend.repositories.PropertyBrandRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PropertyBrandService(
    private val propertyBrandRepository: PropertyBrandRepository
) {
    @Transactional(readOnly = true)
    fun getAllPropertyBrands(): List<PropertyBrandResponse> =
        propertyBrandRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getPropertyBrandById(id: Int): PropertyBrandResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return propertyBrandRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found") }
    }

    @Transactional
    fun createPropertyBrand(request: PropertyBrandRequest): PropertyBrandResponse {
        if (request.brandName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "brandName must not be blank")
        }
        val propertyBrand = PropertyBrand(brandName = request.brandName)
        return propertyBrandRepository.save(propertyBrand).toResponse()
    }

    @Transactional
    fun updatePropertyBrand(id: Int, request: PropertyBrandRequest): PropertyBrandResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.brandName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "brandName must not be blank")
        }
        if (!propertyBrandRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }
        val propertyBrand = PropertyBrand(id = id, brandName = request.brandName)
        return propertyBrandRepository.save(propertyBrand).toResponse()
    }

    @Transactional
    fun deletePropertyBrand(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!propertyBrandRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "property brand not found")
        }
        propertyBrandRepository.deleteById(id)
    }
}

private fun PropertyBrand.toResponse(): PropertyBrandResponse =
    PropertyBrandResponse(id = id, brandName = brandName)
