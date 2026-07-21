package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Accessibility CRUD now lives in inventory-service,
 * reached via accessibilityFeignClient.
 */
@Service
class AccessibilityService(private val accessibilityFeignClient: AccessibilityFeignClient) {
    suspend fun getAllAccessibility(): List<Accessibility> = accessibilityFeignClient.list(ids = null)

    suspend fun createAccessibility(request: AccessibilityRequest): Accessibility =
        try {
            accessibilityFeignClient.create(request)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid accessibility")
        }

    suspend fun updateAccessibility(
        id: Int,
        request: AccessibilityRequest,
    ): Accessibility =
        try {
            accessibilityFeignClient.update(id, request)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "accessibility not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid accessibility")
        }

    suspend fun deleteAccessibility(id: Int) {
        try {
            accessibilityFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "accessibility not found")
        }
    }
}
