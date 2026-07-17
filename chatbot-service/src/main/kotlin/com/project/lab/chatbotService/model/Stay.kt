package com.project.lab.chatbotService.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * Enumeration representing the property listing category.
 */
enum class PropertyType {
    HOTEL,
    HOME
}

/**
 * DTO representing a lodging listing (Stay) returned from the backend service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Stay(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("about") val about: String? = null,
    @JsonProperty("propertyType") val propertyType: PropertyType? = null,
    @JsonProperty("isRefundable") val isRefundable: Boolean? = null,
    @JsonProperty("starRating") val starRating: BigDecimal? = null,
    @JsonProperty("daysFromBookingCancellationDeadline") val daysFromBookingCancellationDeadline: Int? = null,
    @JsonProperty("policiesText") val policiesText: String? = null,
    @JsonProperty("importantInformation") val importantInformation: String? = null,
    @JsonProperty("host") val host: Host? = null,
    @JsonProperty("address") val address: Address? = null,
    @JsonProperty("startingFromPrice") val startingFromPrice: BigDecimal? = null,
    @JsonProperty("amenities") val amenities: List<Amenity>? = null,
    @JsonProperty("views") val views: List<View>? = null
)

/**
 * DTO representing a property host profile.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Host(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("communicationRating") val communicationRating: Double? = null,
    @JsonProperty("checkInRating") val checkInRating: Double? = null,
    @JsonProperty("cancellationRate") val cancellationRate: Double? = null,
    @JsonProperty("languagesSpoken") val languagesSpoken: String? = null
)

/**
 * DTO representing physical address elements of a Stay.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("streetAddress") val streetAddress: String? = null,
    @JsonProperty("extendedAddress") val extendedAddress: String? = null,
    @JsonProperty("city") val city: String? = null,
    @JsonProperty("stateProvince") val stateProvince: String? = null,
    @JsonProperty("postalCode") val postalCode: String? = null,
    @JsonProperty("countryCode") val countryCode: String? = null
)

/**
 * DTO representing property amenity categories.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Amenity(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("name") val name: String? = null
)

/**
 * DTO representing property view types.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class View(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("name") val name: String? = null
)
