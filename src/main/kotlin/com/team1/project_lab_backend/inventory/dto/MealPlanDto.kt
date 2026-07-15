package com.team1.project_lab_backend.inventory.dto

data class MealPlanRequest(
    val mealPlanType: String,
)

data class MealPlanResponse(
    val id: Int,
    val mealPlanType: String,
)
