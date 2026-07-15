package com.team1.project_lab_backend.inventory.dto

data class PropertyBrandRequest(
    val brandName: String,
)

data class PropertyBrandResponse(
    val id: Int,
    val brandName: String,
)
