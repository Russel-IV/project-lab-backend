package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.View
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): View CRUD now lives in inventory-service,
 * reached via viewFeignClient.
 */
@Service
class ViewService(private val viewFeignClient: ViewFeignClient) {
    fun getAllViews(): List<View> = viewFeignClient.list(ids = null)

    fun createView(request: ViewRequest): View =
        try {
            viewFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid view")
        }

    fun updateView(
        id: Int,
        request: ViewRequest,
    ): View =
        try {
            viewFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid view")
        }

    fun deleteView(id: Int) {
        try {
            viewFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        }
    }
}
