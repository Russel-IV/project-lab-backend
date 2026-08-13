package com.team1.project_lab_backend.inventory.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "region")
open class Region(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    @Column(name = "city", nullable = false, columnDefinition = "TEXT")
    open val city: String,
    @Column(name = "country_code", nullable = false, length = 2)
    open val countryCode: String,
    @Column(name = "state_province", columnDefinition = "TEXT")
    open val stateProvince: String? = null,
    @Column(name = "curated_rank")
    open val curatedRank: Int? = null,
)
