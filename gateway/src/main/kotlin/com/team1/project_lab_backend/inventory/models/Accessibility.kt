package com.team1.project_lab_backend.inventory.models

/** Owned by inventory-service (docs/adr/0002, Phase 5) — GraphQL-facing DTO, not a JPA entity. */
data class Accessibility(
    val id: Int,
    val accessibilityType: String,
)
