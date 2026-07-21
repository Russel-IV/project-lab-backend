package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Region
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RegionRepository : JpaRepository<Region, Int> {
    fun findByCityIgnoreCaseAndCountryCodeIgnoreCase(
        city: String,
        countryCode: String,
    ): Region?

    // ADR-0020/0023: null search returns everything (up to limit) unfiltered — the
    // additive path the old no-arg destinations() collapses into. unaccent/similarity
    // are Postgres extension functions (V3) with no JPQL equivalent, hence nativeQuery.
    // Casting :search explicitly avoids Postgres failing to infer a type for it when
    // it's only ever seen inside a function call.
    @Query(
        value = """
            SELECT * FROM region
            WHERE (:search IS NULL)
               OR (
                    unaccent(lower(city)) ILIKE '%' || unaccent(lower(CAST(:search AS text))) || '%'
                    OR similarity(city, CAST(:search AS text)) > 0.3
                  )
            ORDER BY city
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun search(
        @Param("search") search: String?,
        @Param("limit") limit: Int,
    ): List<Region>
}
