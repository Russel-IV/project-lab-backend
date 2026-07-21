package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DestinationServiceTest {
    private val destinationFeignClient = Mockito.mock(DestinationFeignClient::class.java)
    private val destinationService = DestinationService(destinationFeignClient)

    @Test
    fun searchDestinationsDelegatesToFeignClient() {
        val destinations = listOf(Destination(city = "Paris", countryCode = "FR", regionId = 1))
        Mockito.`when`(destinationFeignClient.list("Par", 5)).thenReturn(destinations)

        val result = destinationService.searchDestinations("Par", 5)

        assertEquals(destinations, result)
    }

    @Test
    fun searchDestinationsPassesNullSearchThrough() {
        val destinations = listOf(Destination(city = "Miami", countryCode = "US", regionId = 2))
        Mockito.`when`(destinationFeignClient.list(null, 20)).thenReturn(destinations)

        val result = destinationService.searchDestinations(null, 20)

        assertEquals(destinations, result)
    }

    @Test
    fun popularDestinationsDelegatesToFeignClient() {
        val destinations = listOf(Destination(city = "Tokyo", countryCode = "JP", regionId = 3))
        Mockito.`when`(destinationFeignClient.popular(8)).thenReturn(destinations)

        val result = destinationService.popularDestinations(8)

        assertEquals(destinations, result)
    }
}
