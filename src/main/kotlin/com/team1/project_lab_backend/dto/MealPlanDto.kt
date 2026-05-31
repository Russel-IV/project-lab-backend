package com.team1.project_lab_backend.dto

data class MealPlanRequest(
    val mealPlanType: String
)

data class MealPlanResponse(
    val id: Int,
    val mealPlanType: String
)
