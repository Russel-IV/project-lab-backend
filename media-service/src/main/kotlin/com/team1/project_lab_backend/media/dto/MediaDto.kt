package com.team1.project_lab_backend.media.dto

data class MediaResponse(
    val id: Int,
    val ownerType: String,
    val ownerId: Int,
    val url: String,
    val thumbnailUrl: String,
    val url1024: String?,
    val url768: String?,
    val url512: String?,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)

data class UpdateMediaRequest(
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)
