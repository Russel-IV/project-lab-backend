package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AccessibilityRequest
import com.team1.project_lab_backend.dto.AccessibilityResponse
import com.team1.project_lab_backend.models.Accessibility
import com.team1.project_lab_backend.repositories.AccessibilityRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccessibilityService(
    private val accessibilityRepository: AccessibilityRepository
) {
    @Transactional(readOnly = true)
    fun getAllAccessibility(): List<AccessibilityResponse> =
        accessibilityRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getAccessibilityById(id: Int): AccessibilityResponse {
        id.requirePositive()
        return accessibilityRepository.findById(id).orNotFound("accessibility not found").toResponse()
    }

    @Transactional
    fun createAccessibility(request: AccessibilityRequest): AccessibilityResponse {
        request.accessibilityType.requireNotBlank("accessibilityType")
        val accessibility = Accessibility(accessibilityType = request.accessibilityType)
        return accessibilityRepository.save(accessibility).toResponse()
    }

    @Transactional
    fun updateAccessibility(id: Int, request: AccessibilityRequest): AccessibilityResponse {
        id.requirePositive()
        request.accessibilityType.requireNotBlank("accessibilityType")
        accessibilityRepository.requireExistsById(id, "accessibility not found")
        val accessibility = Accessibility(id = id, accessibilityType = request.accessibilityType)
        return accessibilityRepository.save(accessibility).toResponse()
    }

    @Transactional
    fun deleteAccessibility(id: Int) {
        id.requirePositive()
        accessibilityRepository.requireExistsById(id, "accessibility not found")
        accessibilityRepository.deleteById(id)
    }
}

private fun Accessibility.toResponse(): AccessibilityResponse =
    AccessibilityResponse(id = id, accessibilityType = accessibilityType)
