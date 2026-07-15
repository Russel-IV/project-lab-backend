package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.models.MealPlan
import com.team1.project_lab_backend.inventory.repositories.MealPlanRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MealPlanService(
    private val mealPlanRepository: MealPlanRepository,
) {
    @Transactional(readOnly = true)
    fun getAllMealPlans(): List<MealPlan> = mealPlanRepository.findAll()

    @Transactional(readOnly = true)
    fun getMealPlanById(id: Int): MealPlan {
        id.requirePositive()
        return mealPlanRepository.findById(id).orNotFound("meal plan not found")
    }

    @Transactional
    fun createMealPlan(request: MealPlanRequest): MealPlan {
        request.mealPlanType.requireNotBlank("mealPlanType")
        return mealPlanRepository.save(MealPlan(mealPlanType = request.mealPlanType))
    }

    @Transactional
    fun updateMealPlan(
        id: Int,
        request: MealPlanRequest,
    ): MealPlan {
        id.requirePositive()
        request.mealPlanType.requireNotBlank("mealPlanType")
        mealPlanRepository.requireExistsById(id, "meal plan not found")
        return mealPlanRepository.save(MealPlan(id = id, mealPlanType = request.mealPlanType))
    }

    @Transactional
    fun deleteMealPlan(id: Int) {
        id.requirePositive()
        mealPlanRepository.requireExistsById(id, "meal plan not found")
        mealPlanRepository.deleteById(id)
    }
}
