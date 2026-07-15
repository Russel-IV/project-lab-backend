package com.team1.project_lab_backend.inventory.models

enum class AmenityType {
    ROOM_AMENITY, PROPERTY_AMENITY
}

/** Owned by inventory-service (docs/adr/0002, Phase 5) — GraphQL-facing DTO, not a JPA entity. */
data class Amenity(
    val id: Int,
    val name: String,
    val type: AmenityType,
)
