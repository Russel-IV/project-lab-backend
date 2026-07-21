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
    // Not a JTS Point (docs/adr/0010, Phase 5) — this DTO now crosses a real HTTP/JSON
    // boundary (the Gateway's Feign client), and Point has no Jackson mapping without
    // an extra datatype module this project doesn't otherwise need. StayService.
    // buildStay() builds the Point server-side, same GeometryFactory conversion
    // StayResolver.LocationInput.toPoint() used to do only in the Gateway.
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class StayFilter(
    val city: String? = null,
    val countryCode: String? = null,
    val regionId: Int? = null,
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

/**
 * Stay itself (unlike Room/Amenity/etc.) can't be serialized as the raw JPA entity:
 * address/propertyBrand/amenities/views/... are lazy relations, and by the time
 * Jackson writes the HTTP response the @Transactional session that could load them is
 * already closed. StayService.toResponse() maps this while the session is still open;
 * every relation the Gateway's batch resolvers need travels as a plain id (or, for
 * the always-needed 1:1 address, nested directly) rather than a nested object graph —
 * the Gateway resolves those ids to full objects itself via its own bulk Feign calls,
 * same shape as every other cross-service relation in this migration.
 */
data class StayResponse(
    val id: Int,
    val name: String,
    val about: String?,
    val propertyType: PropertyType,
    val isRefundable: Boolean,
    val starRating: BigDecimal?,
    val daysFromBookingCancellationDeadline: Int?,
    val policiesText: String?,
    val importantInformation: String?,
    val hostId: Int,
    val propertyBrandId: Int?,
    val address: AddressResponse,
    val viewIds: Set<Int>,
    val amenityIds: Set<Int>,
    val accessibilityIds: Set<Int>,
    val mealPlanIds: Set<Int>,
    val paymentTypeIds: Set<Int>,
    val travelerExperienceIds: Set<Int>,
    val latitude: Double?,
    val longitude: Double?,
)
