package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.repositories.RegionRepository
import com.team1.project_lab_backend.inventory.repositories.RegionSearchResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DestinationServiceTest {
    private val regionRepository = Mockito.mock(RegionRepository::class.java)
    private val destinationService = DestinationService(regionRepository)

    private fun regionResult(
        id: Int,
        city: String,
        countryCode: String = "US",
        stayCount: Long = 0,
    ) = object : RegionSearchResult {
        override val id: Int = id
        override val city: String = city
        override val countryCode: String = countryCode
        override val stayCount: Long = stayCount
    }

    @Test
    fun searchDestinationsMapsRegionsToDestinations() {
        Mockito.`when`(regionRepository.search(null)).thenReturn(
            listOf(
                regionResult(id = 2, city = "Miami", countryCode = "US", stayCount = 1),
                regionResult(id = 1, city = "Paris", countryCode = "FR", stayCount = 1),
            ),
        )

        val result = destinationService.searchDestinations(null, 20)

        assertEquals(2, result.size)
        assertEquals("Miami", result[0].city)
        assertEquals("US", result[0].countryCode)
        assertEquals(2, result[0].regionId)
        assertEquals("Paris", result[1].city)
        assertEquals("FR", result[1].countryCode)
        assertEquals(1, result[1].regionId)
    }

    @Test
    fun searchDestinationsReturnsEmptyListWhenNoRegionsMatch() {
        Mockito.`when`(regionRepository.search("Nowhere")).thenReturn(emptyList())

        val result = destinationService.searchDestinations("Nowhere", 20)

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun searchDestinationsPassesSearchToRepositoryAndAppliesLimitAfterRanking() {
        Mockito.`when`(regionRepository.search("Par")).thenReturn(
            listOf(
                regionResult(id = 1, city = "Paris", stayCount = 1),
                regionResult(id = 2, city = "Parma", stayCount = 1),
                regionResult(id = 3, city = "Park City", stayCount = 1),
            ),
        )

        val result = destinationService.searchDestinations("Par", 2)

        assertEquals(2, result.size)
        Mockito.verify(regionRepository).search("Par")
    }

    // ---- ranking (ADR-0021) ----

    @Test
    fun rankingPutsPrefixMatchBeforeMidStringMatchRegardlessOfStayCount() {
        Mockito.`when`(regionRepository.search("San")).thenReturn(
            listOf(
                // mid-string match ("...San...") with a huge stay count
                regionResult(id = 1, city = "New Santiago", stayCount = 1000),
                // prefix match with a much smaller stay count
                regionResult(id = 2, city = "Santiago", stayCount = 1),
            ),
        )

        val result = destinationService.searchDestinations("San", 20)

        assertEquals("Santiago", result[0].city)
        assertEquals("New Santiago", result[1].city)
    }

    @Test
    fun rankingOrdersByStayCountDescendingWhenPrefixTierTies() {
        Mockito.`when`(regionRepository.search(null)).thenReturn(
            listOf(
                regionResult(id = 1, city = "Paris", stayCount = 5),
                regionResult(id = 2, city = "Amsterdam", stayCount = 50),
            ),
        )

        val result = destinationService.searchDestinations(null, 20)

        assertEquals("Amsterdam", result[0].city)
        assertEquals("Paris", result[1].city)
    }

    @Test
    fun rankingFallsBackToAlphabeticalWhenPrefixAndStayCountTie() {
        Mockito.`when`(regionRepository.search(null)).thenReturn(
            listOf(
                regionResult(id = 1, city = "Zermatt", stayCount = 1),
                regionResult(id = 2, city = "Amsterdam", stayCount = 1),
            ),
        )

        val result = destinationService.searchDestinations(null, 20)

        assertEquals("Amsterdam", result[0].city)
        assertEquals("Zermatt", result[1].city)
    }

    @Test
    fun rankingTreatsPrefixMatchAsDiacriticAndCaseInsensitive() {
        Mockito.`when`(regionRepository.search("valpara")).thenReturn(
            listOf(
                // not a prefix match, but a huge stay count
                regionResult(id = 1, city = "Nueva Valparaíso", stayCount = 1000),
                // prefix match only once diacritics are folded and case is ignored
                regionResult(id = 2, city = "Valparaíso", stayCount = 1),
            ),
        )

        val result = destinationService.searchDestinations("valpara", 20)

        assertEquals("Valparaíso", result[0].city)
        assertEquals("Nueva Valparaíso", result[1].city)
    }
}
