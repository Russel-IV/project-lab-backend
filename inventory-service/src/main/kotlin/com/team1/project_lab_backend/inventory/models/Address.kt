package com.team1.project_lab_backend.inventory.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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
    open val countryCode: String,
    // Stable identifier for the (city, country_code) pair (docs/adr/0018) — resolved/
    // created by StayService.findOrCreateRegion(), not supplied directly by callers.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    open val region: Region,
)
