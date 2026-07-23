package com.team1.project_lab_backend.media.models

/**
 * Owned by media-service (docs/adr/0003) as a generic Media(ownerType=ROOM, ...) row —
 * this is just the GraphQL-facing DTO shape, not a JPA entity.
 */
data class RoomPicture(
    val id: Int,
    val roomId: Int,
    val url: String,
    val thumbnailUrl: String,
    val url1024: String? = null,
    val url512: String? = null,
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
