package com.team1.project_lab_backend.inventory.models

import java.math.BigDecimal

/**
 * Owned by inventory-service (docs/adr/0002, Phase 5) — this is just the
 * GraphQL-facing DTO shape, not a JPA entity. pictures is Feign-fetched independently
 * by media-service; amenities is batch-resolved from amenityIds via RoomBatchResolver,
 * same pattern as Stay.amenities/StayBatchResolver.
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
    val amenityIds: Set<Int> = emptySet(),
)
