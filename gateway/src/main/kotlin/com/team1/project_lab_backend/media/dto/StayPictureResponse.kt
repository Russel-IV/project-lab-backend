package com.team1.project_lab_backend.media.dto

data class StayPictureResponse(
    val id: Int,
    val stayId: Int,
    val url: String,
    val thumbnailUrl: String,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)
