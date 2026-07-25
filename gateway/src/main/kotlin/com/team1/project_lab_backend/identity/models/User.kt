package com.team1.project_lab_backend.identity.models

import java.util.UUID

/**
 * Owned by identity-service (docs/adr/0002, Phase 4) — this is just the GraphQL-facing
 * DTO shape, not a JPA entity. passwordHash never appears here at all now (rather than
 * being present-but-@JsonIgnore'd as before): Gateway has no reason to ever hold it.
 */
data class User(
    val id: Int,
    val publicId: UUID,
    val name: String,
    val email: String?,
)
