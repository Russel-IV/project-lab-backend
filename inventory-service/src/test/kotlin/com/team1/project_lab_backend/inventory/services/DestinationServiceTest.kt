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
    fun getAllDestinationsMapsRegionsToDestinations() {
        Mockito.`when`(regionRepository.findAllByOrderByCityAsc()).thenReturn(
            listOf(
                Region(id = 2, city = "Miami", countryCode = "US"),
                Region(id = 1, city = "Paris", countryCode = "FR"),
            ),
        )

        val result = destinationService.getAllDestinations()

        assertEquals(2, result.size)
        assertEquals("Miami", result[0].city)
        assertEquals("US", result[0].countryCode)
        assertEquals(2, result[0].regionId)
        assertEquals("Paris", result[1].city)
        assertEquals("FR", result[1].countryCode)
        assertEquals(1, result[1].regionId)
    }

    @Test
    fun getAllDestinationsReturnsEmptyListWhenNoRegionsExist() {
        Mockito.`when`(regionRepository.findAllByOrderByCityAsc()).thenReturn(emptyList())

        val result = destinationService.getAllDestinations()

        assertEquals(emptyList<Any>(), result)
    }
}
