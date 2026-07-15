package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.models.MealPlan
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): MealPlan CRUD now lives in inventory-service,
 * reached via mealPlanFeignClient.
 */
@Service
class MealPlanService(private val mealPlanFeignClient: MealPlanFeignClient) {

    fun getAllMealPlans(): List<MealPlan> = mealPlanFeignClient.list(ids = null)

    fun createMealPlan(request: MealPlanRequest): MealPlan =
        try {
            mealPlanFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid meal plan")
        }

    fun updateMealPlan(id: Int, request: MealPlanRequest): MealPlan =
        try {
            mealPlanFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "meal plan not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid meal plan")
        }

    fun deleteMealPlan(id: Int) {
        try {
            mealPlanFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "meal plan not found")
        }
    }
}
