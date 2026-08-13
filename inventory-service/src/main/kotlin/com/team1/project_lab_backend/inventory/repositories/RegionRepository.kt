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

    @Query(
        value = """
            SELECT r.id AS id, r.city AS city, r.country_code AS countryCode,
                   r.curated_rank AS curatedRank, COUNT(s.id) AS stayCount
            FROM region r
            LEFT JOIN address a ON a.region_id = r.id
            LEFT JOIN stay s ON s.address_id = a.id
            WHERE (:search IS NULL)
               OR (
                    unaccent(lower(r.city)) ILIKE '%' || unaccent(lower(CAST(:search AS text))) || '%'
                    OR similarity(r.city, CAST(:search AS text)) > 0.3
                  )
            GROUP BY r.id, r.city, r.country_code, r.curated_rank
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
    val curatedRank: Int?
    val stayCount: Long
}
