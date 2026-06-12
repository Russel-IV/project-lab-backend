package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ViewRequest
import com.team1.project_lab_backend.dto.ViewResponse
import com.team1.project_lab_backend.models.View
import com.team1.project_lab_backend.repositories.ViewRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ViewService(
    private val viewRepository: ViewRepository
) {
    @Transactional(readOnly = true)
    fun getAllViews(): List<ViewResponse> =
        viewRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getViewById(id: Int): ViewResponse {
        id.requirePositive()
        return viewRepository.findById(id).orNotFound("view not found").toResponse()
    }

    @Transactional
    fun createView(request: ViewRequest): ViewResponse {
        request.viewType.requireNotBlank("viewType")
        val view = View(viewType = request.viewType)
        return viewRepository.save(view).toResponse()
    }

    @Transactional
    fun updateView(id: Int, request: ViewRequest): ViewResponse {
        id.requirePositive()
        request.viewType.requireNotBlank("viewType")
        viewRepository.requireExistsById(id, "view not found")
        val view = View(id = id, viewType = request.viewType)
        return viewRepository.save(view).toResponse()
    }

    @Transactional
    fun deleteView(id: Int) {
        id.requirePositive()
        viewRepository.requireExistsById(id, "view not found")
        viewRepository.deleteById(id)
    }
}

private fun View.toResponse(): ViewResponse =
    ViewResponse(id = id, viewType = viewType)
