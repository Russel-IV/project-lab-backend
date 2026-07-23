package com.team1.project_lab_backend.media.models

/**
 * Owned by media-service (docs/adr/0003) as a generic Media(ownerType=STAY, ...) row —
 * this is just the GraphQL-facing DTO shape, not a JPA entity.
 */
data class StayPicture(
    val id: Int,
    val stayId: Int,
    val url: String,
    val thumbnailUrl: String,
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
