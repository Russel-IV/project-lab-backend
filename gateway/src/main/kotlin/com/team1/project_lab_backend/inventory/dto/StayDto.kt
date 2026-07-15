package com.team1.project_lab_backend.inventory.dto

import com.team1.project_lab_backend.inventory.models.PropertyType
import java.math.BigDecimal
import java.time.LocalDate

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
    val travelerExperienceIds: Set<Int> = emptySet(),
    // Not a JTS Point (docs/adr/0010, Phase 5) — this DTO is now sent as the Feign
    // request body to inventory-service, and Point has no Jackson mapping without an
    // extra datatype module this project doesn't otherwise need.
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class StayFilter(
    val city: String? = null,
    val countryCode: String? = null,
    val propertyType: PropertyType? = null,
    val minPricePerNight: BigDecimal? = null,
    val maxPricePerNight: BigDecimal? = null,
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guests: Int? = null,
    val starRatings: List<Int>? = null,
    val bedrooms: List<Int>? = null,
    val propertyAmenityIds: List<Int>? = null,
    val roomAmenityIds: List<Int>? = null,
)
