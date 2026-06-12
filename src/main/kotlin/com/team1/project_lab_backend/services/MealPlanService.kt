package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.MealPlanRequest
import com.team1.project_lab_backend.dto.MealPlanResponse
import com.team1.project_lab_backend.models.MealPlan
import com.team1.project_lab_backend.repositories.MealPlanRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MealPlanService(
    private val mealPlanRepository: MealPlanRepository
) {
    @Transactional(readOnly = true)
    fun getAllMealPlans(): List<MealPlanResponse> =
        mealPlanRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getMealPlanById(id: Int): MealPlanResponse {
        id.requirePositive()
        return mealPlanRepository.findById(id).orNotFound("meal plan not found").toResponse()
    }

    @Transactional
    fun createMealPlan(request: MealPlanRequest): MealPlanResponse {
        request.mealPlanType.requireNotBlank("mealPlanType")
        val mealPlan = MealPlan(mealPlanType = request.mealPlanType)
        return mealPlanRepository.save(mealPlan).toResponse()
    }

    @Transactional
    fun updateMealPlan(id: Int, request: MealPlanRequest): MealPlanResponse {
        id.requirePositive()
        request.mealPlanType.requireNotBlank("mealPlanType")
        mealPlanRepository.requireExistsById(id, "meal plan not found")
        val mealPlan = MealPlan(id = id, mealPlanType = request.mealPlanType)
        return mealPlanRepository.save(mealPlan).toResponse()
    }

    @Transactional
    fun deleteMealPlan(id: Int) {
        id.requirePositive()
        mealPlanRepository.requireExistsById(id, "meal plan not found")
        mealPlanRepository.deleteById(id)
    }
}

private fun MealPlan.toResponse(): MealPlanResponse =
    MealPlanResponse(id = id, mealPlanType = mealPlanType)
