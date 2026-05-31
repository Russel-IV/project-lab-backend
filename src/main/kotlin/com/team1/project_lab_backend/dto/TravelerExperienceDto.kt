package com.team1.project_lab_backend.dto

data class TravelerExperienceRequest(
    val travelerExperienceType: String
)

data class TravelerExperienceResponse(
    val id: Int,
    val travelerExperienceType: String
)
