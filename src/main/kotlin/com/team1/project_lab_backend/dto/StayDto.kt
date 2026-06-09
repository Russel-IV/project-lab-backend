package com.team1.project_lab_backend.dto

import com.team1.project_lab_backend.models.PropertyType
import java.math.BigDecimal

data class StayRequest(
    val name: String,
    val about: String? = null,
    val propertyType: PropertyType,
    val address: AddressRequest,
    val isRefundable: Boolean = false,
    val starRating: BigDecimal? = null,
    val daysFromBookingCancellationDeadline: Int? = null,
    val policiesText: String? = null,
    val importantInformation: String? = null,
    val hostId: Int,
    val propertyBrandId: Int? = null,
    val viewIds: Set<Int> = emptySet(),
    val amenityIds: Set<Int> = emptySet(),
    val accessibilityIds: Set<Int> = emptySet(),
    val mealPlanIds: Set<Int> = emptySet(),
    val paymentTypeIds: Set<Int> = emptySet(),
    val travelerExperienceIds: Set<Int> = emptySet()
)

data class StayResponse(
    val id: Int,
    val name: String,
    val about: String?,
    val propertyType: PropertyType,
    val address: AddressResponse,
    val isRefundable: Boolean,
    val starRating: BigDecimal?,
    val daysFromBookingCancellationDeadline: Int?,
    val policiesText: String?,
    val importantInformation: String?,
    val hostId: Int,
    val propertyBrandId: Int?,
    val viewIds: Set<Int>,
    val amenityIds: Set<Int>,
    val accessibilityIds: Set<Int>,
    val mealPlanIds: Set<Int>,
    val paymentTypeIds: Set<Int>,
    val travelerExperienceIds: Set<Int>,
    val rooms: List<RoomResponse>,
    val pictures: List<StayPictureResponse>,
    val startingFromPrice: BigDecimal?
)
