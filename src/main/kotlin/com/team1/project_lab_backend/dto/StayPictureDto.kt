package com.team1.project_lab_backend.dto

data class StayPictureRequest(
    val url: String,
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)

data class StayPictureResponse(
    val id: Int,
    val stayId: Int,
    val url: String,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int
)
