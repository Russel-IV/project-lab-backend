package com.team1.project_lab_backend.inventory.models

/**
 * Owned by inventory-service (docs/adr/0002, Phase 5) — this is just the
 * GraphQL-facing DTO shape, not a JPA entity. Travels nested inside Stay (see
 * Stay.kt's kdoc) rather than being resolved via its own Feign client.
 */
data class Address(
    val id: Int,
    val streetAddress: String,
    val extendedAddress: String? = null,
    val city: String,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val countryCode: String,
)
