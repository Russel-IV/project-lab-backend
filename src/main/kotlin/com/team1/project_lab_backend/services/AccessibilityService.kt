package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AccessibilityRequest
import com.team1.project_lab_backend.dto.AccessibilityResponse
import com.team1.project_lab_backend.models.Accessibility
import com.team1.project_lab_backend.repositories.AccessibilityRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AccessibilityService(
    private val accessibilityRepository: AccessibilityRepository
) {
    @Transactional(readOnly = true)
    fun getAllAccessibility(): List<AccessibilityResponse> =
        accessibilityRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getAccessibilityById(id: Int): AccessibilityResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return accessibilityRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "accessibility not found") }
    }

    @Transactional
    fun createAccessibility(request: AccessibilityRequest): AccessibilityResponse {
        if (request.accessibilityType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "accessibilityType must not be blank")
        }
        val accessibility = Accessibility(accessibilityType = request.accessibilityType)
        return accessibilityRepository.save(accessibility).toResponse()
    }

    @Transactional
    fun updateAccessibility(id: Int, request: AccessibilityRequest): AccessibilityResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.accessibilityType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "accessibilityType must not be blank")
        }
        if (!accessibilityRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "accessibility not found")
        }
        val accessibility = Accessibility(id = id, accessibilityType = request.accessibilityType)
        return accessibilityRepository.save(accessibility).toResponse()
    }

    @Transactional
    fun deleteAccessibility(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!accessibilityRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "accessibility not found")
        }
        accessibilityRepository.deleteById(id)
    }
}

private fun Accessibility.toResponse(): AccessibilityResponse =
    AccessibilityResponse(id = id, accessibilityType = accessibilityType)
