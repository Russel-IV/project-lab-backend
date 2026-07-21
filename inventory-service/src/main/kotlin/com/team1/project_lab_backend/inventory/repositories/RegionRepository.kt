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

    // ADR-0020/0023: null search returns everything unfiltered — the additive path the
    // old no-arg destinations() collapses into. unaccent/similarity are Postgres
    // extension functions (V3) with no JPQL equivalent, hence nativeQuery. Casting
    // :search explicitly avoids Postgres failing to infer a type for it when it's only
    // ever seen inside a function call.
    //
    // No ORDER BY/LIMIT here (unlike Phase 3) — ADR-0021 puts ranking in
    // DestinationService specifically so it's unit-testable without a live Postgres,
    // so this only fetches candidates + the stay_count each needs to be ranked on;
    // DestinationService sorts and truncates to `limit` itself. Each address has at
    // most one stay (address_id is UNIQUE on stay), so COUNT(s.id) needs no DISTINCT.
    @Query(
        value = """
            SELECT r.id AS id, r.city AS city, r.country_code AS countryCode, COUNT(s.id) AS stayCount
            FROM region r
            LEFT JOIN address a ON a.region_id = r.id
            LEFT JOIN stay s ON s.address_id = a.id
            WHERE (:search IS NULL)
               OR (
                    unaccent(lower(r.city)) ILIKE '%' || unaccent(lower(CAST(:search AS text))) || '%'
                    OR similarity(r.city, CAST(:search AS text)) > 0.3
                  )
            GROUP BY r.id, r.city, r.country_code
        """,
        nativeQuery = true,
    )
    fun search(
        @Param("search") search: String?,
    ): List<RegionSearchResult>
}

interface RegionSearchResult {
    val id: Int
    val city: String
    val countryCode: String
    val stayCount: Long
}
