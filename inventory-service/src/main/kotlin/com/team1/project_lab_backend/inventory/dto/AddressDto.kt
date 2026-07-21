package com.team1.project_lab_backend.inventory.dto

data class AddressRequest(
    val streetAddress: String,
    val extendedAddress: String? = null,
    val city: String,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val countryCode: String,
)

data class AddressResponse(
    val id: Int,
    val streetAddress: String,
    val extendedAddress: String?,
    val city: String,
    val stateProvince: String?,
    val postalCode: String?,
    val countryCode: String,
    val regionId: Int,
)
