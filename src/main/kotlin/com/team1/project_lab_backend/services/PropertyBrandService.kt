package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.PropertyBrandRequest
import com.team1.project_lab_backend.dto.PropertyBrandResponse
import com.team1.project_lab_backend.models.PropertyBrand
import com.team1.project_lab_backend.repositories.PropertyBrandRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PropertyBrandService(
    private val propertyBrandRepository: PropertyBrandRepository
) {
    @Transactional(readOnly = true)
    fun getAllPropertyBrands(): List<PropertyBrandResponse> =
        propertyBrandRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getPropertyBrandById(id: Int): PropertyBrandResponse {
        id.requirePositive()
        return propertyBrandRepository.findById(id).orNotFound("property brand not found").toResponse()
    }

    @Transactional
    fun createPropertyBrand(request: PropertyBrandRequest): PropertyBrandResponse {
        request.brandName.requireNotBlank("brandName")
        val propertyBrand = PropertyBrand(brandName = request.brandName)
        return propertyBrandRepository.save(propertyBrand).toResponse()
    }

    @Transactional
    fun updatePropertyBrand(id: Int, request: PropertyBrandRequest): PropertyBrandResponse {
        id.requirePositive()
        request.brandName.requireNotBlank("brandName")
        propertyBrandRepository.requireExistsById(id, "property brand not found")
        val propertyBrand = PropertyBrand(id = id, brandName = request.brandName)
        return propertyBrandRepository.save(propertyBrand).toResponse()
    }

    @Transactional
    fun deletePropertyBrand(id: Int) {
        id.requirePositive()
        propertyBrandRepository.requireExistsById(id, "property brand not found")
        propertyBrandRepository.deleteById(id)
    }
}

private fun PropertyBrand.toResponse(): PropertyBrandResponse =
    PropertyBrandResponse(id = id, brandName = brandName)
