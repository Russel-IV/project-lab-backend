package com.team1.project_lab_backend.inventory.dto

data class ViewRequest(
    val viewType: String,
)

data class ViewResponse(
    val id: Int,
    val viewType: String,
)
