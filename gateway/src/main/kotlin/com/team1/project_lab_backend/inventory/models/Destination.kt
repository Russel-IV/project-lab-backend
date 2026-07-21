package com.team1.project_lab_backend.inventory.models

/** Owned by inventory-service (docs/adr/0002, Phase 5) — GraphQL-facing DTO shape, not a JPA entity. */
data class Destination(val city: String, val countryCode: String, val regionId: Int)
