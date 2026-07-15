package com.team1.project_lab_backend.inventory.dto

data class AccessibilityRequest(
    val accessibilityType: String,
)

data class AccessibilityResponse(
    val id: Int,
    val accessibilityType: String,
)
