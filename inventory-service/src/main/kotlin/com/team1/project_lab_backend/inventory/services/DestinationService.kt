package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import com.team1.project_lab_backend.inventory.repositories.RegionRepository
import com.team1.project_lab_backend.inventory.repositories.RegionSearchResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer

@Service
class DestinationService(
    private val regionRepository: RegionRepository,
) {
    @Transactional(readOnly = true)
    fun searchDestinations(
        search: String?,
        limit: Int,
    ): List<Destination> =
        regionRepository.search(search)
            .sortedWith(rankingComparator(search))
            .take(limit)
            .map { Destination(city = it.city, countryCode = it.countryCode, regionId = it.id) }

    // ADR-0021: prefix match first, then descending stay_count, then alphabetical.
    // Kept here (rather than folded into the SQL) specifically so it's unit-testable
    // without a live Postgres, independent of future ranking sophistication.
    private fun rankingComparator(search: String?): Comparator<RegionSearchResult> =
        compareByDescending<RegionSearchResult> { isPrefixMatch(it.city, search) }
            .thenByDescending { it.stayCount }
            .thenBy { it.city.lowercase() }

    // Diacritic-folded so a search like "Valparaiso" still ranks "Valparaíso" as a
    // prefix match — consistent with the DB query's own unaccent()-based matching.
    private fun isPrefixMatch(
        city: String,
        search: String?,
    ): Boolean = search != null && foldToAscii(city).startsWith(foldToAscii(search), ignoreCase = true)

    private fun foldToAscii(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD).replace(DIACRITICS_REGEX, "")

    companion object {
        private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    }
}
