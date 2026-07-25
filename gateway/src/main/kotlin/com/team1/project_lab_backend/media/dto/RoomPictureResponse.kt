package com.team1.project_lab_backend.media.dto

data class RoomPictureResponse(
    val id: Int,
    val roomId: Int,
    val url: String,
    val thumbnailUrl: String,
    val url1024: String?,
    val url768: String?,
    val url512: String?,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)
