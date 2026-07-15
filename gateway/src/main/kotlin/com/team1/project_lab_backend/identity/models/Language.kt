package com.team1.project_lab_backend.identity.models

/**
 * Owned by identity-service (docs/adr/0002, Phase 4) — this is just the GraphQL-facing
 * DTO shape, not a JPA entity.
 */
data class Language(
    val id: Int,
    val languageName: String,
)
