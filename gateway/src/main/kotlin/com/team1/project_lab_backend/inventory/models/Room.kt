package com.team1.project_lab_backend.inventory.models

import java.math.BigDecimal

/**
 * Owned by inventory-service (docs/adr/0002, Phase 5) — this is just the
 * GraphQL-facing DTO shape, not a JPA entity. Room has no relations of its own (no
 * batch-resolved fields besides pictures, which media-service already Feign-fetches
 * independently), so unlike Stay this mirrors inventory-service's entity 1:1.
 */
data class Room(
    val id: Int,
    val stayId: Int,
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal? = null,
)
