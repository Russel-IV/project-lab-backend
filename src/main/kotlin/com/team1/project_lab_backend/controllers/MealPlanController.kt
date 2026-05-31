package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.MealPlanRequest
import com.team1.project_lab_backend.dto.MealPlanResponse
import com.team1.project_lab_backend.services.MealPlanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/meal-plans")
class MealPlanController(
    private val mealPlanService: MealPlanService
) {
    @GetMapping
    fun getAllMealPlans(): ResponseEntity<List<MealPlanResponse>> =
        ResponseEntity.ok(mealPlanService.getAllMealPlans())

    @GetMapping("/{id}")
    fun getMealPlanById(@PathVariable id: Int): ResponseEntity<MealPlanResponse> =
        ResponseEntity.ok(mealPlanService.getMealPlanById(id))

    @PostMapping
    fun createMealPlan(@RequestBody mealPlan: MealPlanRequest): ResponseEntity<MealPlanResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.createMealPlan(mealPlan))

    @PutMapping("/{id}")
    fun updateMealPlan(@PathVariable id: Int, @RequestBody mealPlan: MealPlanRequest): ResponseEntity<MealPlanResponse> =
        ResponseEntity.ok(mealPlanService.updateMealPlan(id, mealPlan))

    @DeleteMapping("/{id}")
    fun deleteMealPlan(@PathVariable id: Int): ResponseEntity<Unit> =
        mealPlanService.deleteMealPlan(id).let { ResponseEntity.noContent().build() }
}
