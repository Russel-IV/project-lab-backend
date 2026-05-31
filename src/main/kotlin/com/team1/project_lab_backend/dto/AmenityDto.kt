package com.team1.project_lab_backend.dto

import com.team1.project_lab_backend.models.AmenityType

data class AmenityRequest(
    val name: String,
    val type: AmenityType
)

data class AmenityResponse(
    val id: Int,
    val name: String,
    val type: AmenityType
)
