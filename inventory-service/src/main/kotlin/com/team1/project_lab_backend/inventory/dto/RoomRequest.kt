package com.team1.project_lab_backend.inventory.dto

import java.math.BigDecimal

data class RoomRequest(
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal? = null,
    val amenityIds: Set<Int> = emptySet(),
)

/**
 * Room itself can't be serialized as the raw JPA entity now that it has a lazy
 * `amenities` relation: by the time Jackson writes the HTTP response the
 * @Transactional session that could load it is already closed. RoomService.toResponse()
 * maps this while the session is still open, same pattern as Stay.toResponse()/
 * StayResponse.
 */
data class RoomResponse(
    val id: Int,
    val stayId: Int,
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal?,
    val amenityIds: Set<Int>,
)
