package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ViewRequest
import com.team1.project_lab_backend.dto.ViewResponse
import com.team1.project_lab_backend.models.View
import com.team1.project_lab_backend.repositories.ViewRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ViewService(
    private val viewRepository: ViewRepository
) {
    @Transactional(readOnly = true)
    fun getAllViews(): List<ViewResponse> =
        viewRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getViewById(id: Int): ViewResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return viewRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "view not found") }
    }

    @Transactional
    fun createView(request: ViewRequest): ViewResponse {
        if (request.viewType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "viewType must not be blank")
        }
        val view = View(viewType = request.viewType)
        return viewRepository.save(view).toResponse()
    }

    @Transactional
    fun updateView(id: Int, request: ViewRequest): ViewResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.viewType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "viewType must not be blank")
        }
        if (!viewRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        }
        val view = View(id = id, viewType = request.viewType)
        return viewRepository.save(view).toResponse()
    }

    @Transactional
    fun deleteView(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!viewRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "view not found")
        }
        viewRepository.deleteById(id)
    }
}

private fun View.toResponse(): ViewResponse =
    ViewResponse(id = id, viewType = viewType)
