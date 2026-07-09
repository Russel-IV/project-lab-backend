package com.team1.project_lab_backend.dto

data class RoomPictureResponse(
    val id: Int,
    val roomId: Int,
    val url: String,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int
)
