package com.team1.project_lab_backend.inventory.dto

import java.math.BigDecimal

data class RoomRequest(
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal? = null,
)

data class RoomResponse(
    val id: Int,
    val stayId: Int,
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal?,
)
