package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.View
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): View CRUD now lives in inventory-service,
 * reached via viewFeignClient.
 */
@Service
class ViewService(private val viewFeignClient: ViewFeignClient) {
    suspend fun getAllViews(): List<View> = viewFeignClient.list(ids = null)

    suspend fun createView(request: ViewRequest): View =
        try {
            viewFeignClient.create(request)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid view")
        }

    suspend fun updateView(
        id: Int,
        request: ViewRequest,
    ): View =
        try {
            viewFeignClient.update(id, request)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid view")
        }

    suspend fun deleteView(id: Int) {
        try {
            viewFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        }
    }
}
