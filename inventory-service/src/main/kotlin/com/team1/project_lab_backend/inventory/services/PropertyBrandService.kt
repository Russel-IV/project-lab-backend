package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.inventory.repositories.PropertyBrandRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PropertyBrandService(
    private val propertyBrandRepository: PropertyBrandRepository,
) {
    @Transactional(readOnly = true)
    fun getAllPropertyBrands(): List<PropertyBrand> = propertyBrandRepository.findAll()

    @Transactional(readOnly = true)
    fun getAllById(ids: List<Int>): List<PropertyBrand> = propertyBrandRepository.findAllById(ids)

    @Transactional(readOnly = true)
    fun getPropertyBrandById(id: Int): PropertyBrand {
        id.requirePositive()
        return propertyBrandRepository.findById(id).orNotFound("property brand not found")
    }

    @Transactional
    fun createPropertyBrand(request: PropertyBrandRequest): PropertyBrand {
        request.brandName.requireNotBlank("brandName")
        return propertyBrandRepository.save(PropertyBrand(brandName = request.brandName))
    }

    @Transactional
    fun updatePropertyBrand(
        id: Int,
        request: PropertyBrandRequest,
    ): PropertyBrand {
        id.requirePositive()
        request.brandName.requireNotBlank("brandName")
        propertyBrandRepository.requireExistsById(id, "property brand not found")
        return propertyBrandRepository.save(PropertyBrand(id = id, brandName = request.brandName))
    }

    @Transactional
    fun deletePropertyBrand(id: Int) {
        id.requirePositive()
        propertyBrandRepository.requireExistsById(id, "property brand not found")
        propertyBrandRepository.deleteById(id)
    }
}
