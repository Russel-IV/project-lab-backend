package com.team1.project_lab_backend.models

import jakarta.persistence.*

@Entity
@Table(name = "address")
open class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "street_address", nullable = false, columnDefinition = "TEXT")
    open val streetAddress: String,

    @Column(name = "extended_address", columnDefinition = "TEXT")
    open val extendedAddress: String? = null,

    @Column(name = "city", nullable = false, columnDefinition = "TEXT")
    open val city: String,

    @Column(name = "state_province", columnDefinition = "TEXT")
    open val stateProvince: String? = null,

    @Column(name = "postal_code", columnDefinition = "TEXT")
    open val postalCode: String? = null,

    @Column(name = "country_code", nullable = false, length = 2)
    open val countryCode: String
)
