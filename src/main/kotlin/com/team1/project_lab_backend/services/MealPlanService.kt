package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.MealPlanRequest
import com.team1.project_lab_backend.dto.MealPlanResponse
import com.team1.project_lab_backend.models.MealPlan
import com.team1.project_lab_backend.repositories.MealPlanRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class MealPlanService(
    private val mealPlanRepository: MealPlanRepository
) {
    @Transactional(readOnly = true)
    fun getAllMealPlans(): List<MealPlanResponse> =
        mealPlanRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getMealPlanById(id: Int): MealPlanResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return mealPlanRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "meal plan not found") }
    }

    @Transactional
    fun createMealPlan(request: MealPlanRequest): MealPlanResponse {
        if (request.mealPlanType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "mealPlanType must not be blank")
        }
        val mealPlan = MealPlan(mealPlanType = request.mealPlanType)
        return mealPlanRepository.save(mealPlan).toResponse()
    }

    @Transactional
    fun updateMealPlan(id: Int, request: MealPlanRequest): MealPlanResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.mealPlanType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "mealPlanType must not be blank")
        }
        if (!mealPlanRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "meal plan not found")
        }
        val mealPlan = MealPlan(id = id, mealPlanType = request.mealPlanType)
        return mealPlanRepository.save(mealPlan).toResponse()
    }

    @Transactional
    fun deleteMealPlan(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!mealPlanRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "meal plan not found")
        }
        mealPlanRepository.deleteById(id)
    }
}

private fun MealPlan.toResponse(): MealPlanResponse =
    MealPlanResponse(id = id, mealPlanType = mealPlanType)
