package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Region
import org.springframework.data.jpa.repository.JpaRepository

interface RegionRepository : JpaRepository<Region, Int> {
    fun findByCityIgnoreCaseAndCountryCodeIgnoreCase(
        city: String,
        countryCode: String,
    ): Region?

    fun findAllByOrderByCityAsc(): List<Region>
}
