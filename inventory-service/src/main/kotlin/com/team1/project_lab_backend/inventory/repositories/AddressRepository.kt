package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AddressRepository : JpaRepository<Address, Int> {
    @Query(
        "SELECT DISTINCT a.city AS city, a.countryCode AS countryCode " +
            "FROM Address a ORDER BY a.city",
    )
    fun findDistinctCityCountryPairs(): List<CityCountryProjection>
}

interface CityCountryProjection {
    val city: String
    val countryCode: String
}
