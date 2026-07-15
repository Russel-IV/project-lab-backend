package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import com.team1.project_lab_backend.inventory.repositories.AccessibilityRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccessibilityService(
    private val accessibilityRepository: AccessibilityRepository,
) {
    @Transactional(readOnly = true)
    fun getAllAccessibility(): List<Accessibility> = accessibilityRepository.findAll()

    @Transactional(readOnly = true)
    fun getAccessibilityById(id: Int): Accessibility {
        id.requirePositive()
        return accessibilityRepository.findById(id).orNotFound("accessibility not found")
    }

    @Transactional
    fun createAccessibility(request: AccessibilityRequest): Accessibility {
        request.accessibilityType.requireNotBlank("accessibilityType")
        return accessibilityRepository.save(Accessibility(accessibilityType = request.accessibilityType))
    }

    @Transactional
    fun updateAccessibility(
        id: Int,
        request: AccessibilityRequest,
    ): Accessibility {
        id.requirePositive()
        request.accessibilityType.requireNotBlank("accessibilityType")
        accessibilityRepository.requireExistsById(id, "accessibility not found")
        return accessibilityRepository.save(Accessibility(id = id, accessibilityType = request.accessibilityType))
    }

    @Transactional
    fun deleteAccessibility(id: Int) {
        id.requirePositive()
        accessibilityRepository.requireExistsById(id, "accessibility not found")
        accessibilityRepository.deleteById(id)
    }
}
