package com.team1.project_lab_backend.inventory.dto

data class TravelerExperienceRequest(
    val travelerExperienceType: String,
)

data class TravelerExperienceResponse(
    val id: Int,
    val travelerExperienceType: String,
)
