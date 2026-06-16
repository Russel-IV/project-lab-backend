package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ViewRequest
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
    private val viewRepository: ViewRepository,
) {
    @Transactional(readOnly = true)
    fun getAllViews(): List<View> = viewRepository.findAll()

    @Transactional(readOnly = true)
    fun getViewById(id: Int): View {
        id.requirePositive()
        return viewRepository.findById(id).orNotFound("view not found")
    }

    @Transactional
    fun createView(request: ViewRequest): View {
        request.viewType.requireNotBlank("viewType")
        return viewRepository.save(View(viewType = request.viewType))
    }

    @Transactional
    fun updateView(id: Int, request: ViewRequest): View {
        id.requirePositive()
        request.viewType.requireNotBlank("viewType")
        viewRepository.requireExistsById(id, "view not found")
        return viewRepository.save(View(id = id, viewType = request.viewType))
    }

    @Transactional
    fun deleteView(id: Int) {
        id.requirePositive()
        viewRepository.requireExistsById(id, "view not found")
        viewRepository.deleteById(id)
    }
}
