package com.project.lab.chatbotService.model

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
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
@JsonClassDescription("Lodging listing details including property info, location, rules, and pricing")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Stay(
    @JsonProperty("id")
    @JsonPropertyDescription("Unique integer ID of the stay property listing")
    val id: Int? = null,

    @JsonProperty("name")
    @JsonPropertyDescription("Display title or name of the stay property")
    val name: String? = null,

    @JsonProperty("about")
    @JsonPropertyDescription("Full description of the property listing")
    val about: String? = null,

    @JsonProperty("propertyType")
    @JsonPropertyDescription("Category of property: HOTEL or HOME")
    val propertyType: PropertyType? = null,

    @JsonProperty("isRefundable")
    @JsonPropertyDescription("Whether the reservation is eligible for a full refund upon cancellation")
    val isRefundable: Boolean? = null,

    @JsonProperty("starRating")
    @JsonPropertyDescription("Official star rating out of 5 (e.g. 4.5)")
    val starRating: BigDecimal? = null,

    @JsonProperty("daysFromBookingCancellationDeadline")
    @JsonPropertyDescription("Days before check-in by which cancellation is allowed for a full refund")
    val daysFromBookingCancellationDeadline: Int? = null,

    @JsonProperty("policiesText")
    @JsonPropertyDescription("House rules and cancellation policy guidelines")
    val policiesText: String? = null,

    @JsonProperty("importantInformation")
    @JsonPropertyDescription("Key information guests should know before arrival")
    val importantInformation: String? = null,

    @JsonProperty("host")
    @JsonPropertyDescription("Profile details of the host managing the stay")
    val host: Host? = null,

    @JsonProperty("address")
    @JsonPropertyDescription("Physical location address details")
    val address: Address? = null,

    @JsonProperty("startingFromPrice")
    @JsonPropertyDescription("Lowest nightly rate for the stay")
    val startingFromPrice: BigDecimal? = null,

    @JsonProperty("amenities")
    @JsonPropertyDescription("List of property amenities offered (e.g. WiFi, Pool)")
    val amenities: List<Amenity>? = null,

    @JsonProperty("views")
    @JsonPropertyDescription("List of available room view types (e.g. Ocean View)")
    val views: List<View>? = null
)

/**
 * DTO representing a property host profile.
 */
@JsonClassDescription("Host profile and host quality ratings")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Host(
    @JsonProperty("id")
    @JsonPropertyDescription("Host ID")
    val id: Int? = null,

    @JsonProperty("name")
    @JsonPropertyDescription("Host full name or display name")
    val name: String? = null,

    @JsonProperty("communicationRating")
    @JsonPropertyDescription("Communication rating score out of 5")
    val communicationRating: Double? = null,

    @JsonProperty("checkInRating")
    @JsonPropertyDescription("Check-in rating score out of 5")
    val checkInRating: Double? = null,

    @JsonProperty("cancellationRate")
    @JsonPropertyDescription("Cancellation percentage rate")
    val cancellationRate: Double? = null,

    @JsonProperty("languagesSpoken")
    @JsonPropertyDescription("Languages spoken by host")
    val languagesSpoken: String? = null
)

/**
 * DTO representing physical address elements of a Stay.
 */
@JsonClassDescription("Physical address of the property")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(
    @JsonProperty("id")
    @JsonPropertyDescription("Address ID")
    val id: Int? = null,

    @JsonProperty("streetAddress")
    @JsonPropertyDescription("Street address line 1")
    val streetAddress: String? = null,

    @JsonProperty("extendedAddress")
    @JsonPropertyDescription("Apartment, unit, or suite number")
    val extendedAddress: String? = null,

    @JsonProperty("city")
    @JsonPropertyDescription("City name")
    val city: String? = null,

    @JsonProperty("stateProvince")
    @JsonPropertyDescription("State or province name")
    val stateProvince: String? = null,

    @JsonProperty("postalCode")
    @JsonPropertyDescription("ZIP or postal code")
    val postalCode: String? = null,

    @JsonProperty("countryCode")
    @JsonPropertyDescription("Two-letter ISO country code (e.g. US, GB, ES)")
    val countryCode: String? = null
)

/**
 * DTO representing property amenity categories.
 */
@JsonClassDescription("Amenity offered by the stay")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Amenity(
    @JsonProperty("id")
    @JsonPropertyDescription("Amenity ID")
    val id: Int? = null,

    @JsonProperty("name")
    @JsonPropertyDescription("Amenity name (e.g. WiFi, Pool, Air conditioning)")
    val name: String? = null
)

/**
 * DTO representing property view types.
 */
@JsonClassDescription("View type offered by the stay")
@JsonIgnoreProperties(ignoreUnknown = true)
data class View(
    @JsonProperty("id")
    @JsonPropertyDescription("View ID")
    val id: Int? = null,

    @JsonProperty("name")
    @JsonPropertyDescription("View name (e.g. Ocean view, Mountain view)")
    val name: String? = null
)
