package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Region
import com.team1.project_lab_backend.inventory.repositories.RegionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DestinationServiceTest {
    private val regionRepository = Mockito.mock(RegionRepository::class.java)
    private val destinationService = DestinationService(regionRepository)

    @Test
    fun searchDestinationsMapsRegionsToDestinations() {
        Mockito.`when`(regionRepository.search(null, 20)).thenReturn(
            listOf(
                Region(id = 2, city = "Miami", countryCode = "US"),
                Region(id = 1, city = "Paris", countryCode = "FR"),
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
        Mockito.`when`(regionRepository.search("Nowhere", 20)).thenReturn(emptyList())

        val result = destinationService.searchDestinations("Nowhere", 20)

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun searchDestinationsPassesSearchAndLimitToRepository() {
        Mockito.`when`(regionRepository.search("Par", 5)).thenReturn(
            listOf(Region(id = 1, city = "Paris", countryCode = "FR")),
        )

        val result = destinationService.searchDestinations("Par", 5)

        assertEquals(1, result.size)
        Mockito.verify(regionRepository).search("Par", 5)
    }
}
