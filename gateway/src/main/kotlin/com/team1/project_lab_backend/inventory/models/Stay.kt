package com.team1.project_lab_backend.inventory.models

import java.math.BigDecimal

enum class PropertyType {
    HOTEL, HOME
}

/**
 * Owned by inventory-service (docs/adr/0002, docs/adr/0010, Phase 5) — this is just
 * the GraphQL-facing DTO shape, not a JPA entity. Relations resolved via
 * @BatchMapping (host, propertyBrand, rooms, pictures, amenities, views,
 * accessibilities, mealPlans, paymentTypes, travelerExperiences, startingFromPrice)
 * travel here only as ids — StayBatchResolver resolves those ids to full objects via
 * its own bulk Feign calls. address is the one exception (always needed, cheap,
 * genuinely 1:1) and travels nested directly, same as inventory-service's own
 * StayResponse.
 */
data class Stay(
    val id: Int,
    val name: String,
    val about: String? = null,
    val propertyType: PropertyType,
    val isRefundable: Boolean = false,
    val starRating: BigDecimal? = null,
    val daysFromBookingCancellationDeadline: Int? = null,
    val policiesText: String? = null,
    val importantInformation: String? = null,
    val hostId: Int,
    val propertyBrandId: Int? = null,
    val address: Address,
    val viewIds: Set<Int> = emptySet(),
    val amenityIds: Set<Int> = emptySet(),
    val accessibilityIds: Set<Int> = emptySet(),
    val mealPlanIds: Set<Int> = emptySet(),
    val paymentTypeIds: Set<Int> = emptySet(),
    val travelerExperienceIds: Set<Int> = emptySet(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
