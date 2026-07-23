package com.team1.project_lab_backend.media.dto

data class MediaResponse(
    val id: Int,
    val ownerType: String,
    val ownerId: Int,
    val url: String,
    val thumbnailUrl: String,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)

data class UpdateMediaRequest(
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)
