package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.models.MealPlan
import com.team1.project_lab_backend.inventory.services.MealPlanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Internal-only API (docs/adr/0005) — the Gateway's MealPlanFeignClient is the
 * only caller.
 */
@RestController
@RequestMapping("/internal/meal-plans")
class MealPlanController(private val mealPlanService: MealPlanService) {

    @GetMapping
    fun list(@RequestParam(required = false) ids: List<Int>?): List<MealPlan> =
        if (ids != null) mealPlanService.getAllById(ids) else mealPlanService.getAllMealPlans()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): MealPlan = mealPlanService.getMealPlanById(id)

    @PostMapping
    fun create(@RequestBody request: MealPlanRequest): MealPlan = mealPlanService.createMealPlan(request)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: MealPlanRequest): MealPlan =
        mealPlanService.updateMealPlan(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Void> {
        mealPlanService.deleteMealPlan(id)
        return ResponseEntity.noContent().build()
    }
}
