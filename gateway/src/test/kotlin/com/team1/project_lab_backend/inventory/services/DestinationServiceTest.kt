package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DestinationServiceTest {
    private val destinationFeignClient = Mockito.mock(DestinationFeignClient::class.java)
    private val destinationService = DestinationService(destinationFeignClient)

    @Test
    fun getAllDestinationsDelegatesToFeignClient() {
        val destinations = listOf(Destination(city = "Paris", countryCode = "FR"))
        Mockito.`when`(destinationFeignClient.list()).thenReturn(destinations)

        val result = destinationService.getAllDestinations()

        assertEquals(destinations, result)
    }
}
