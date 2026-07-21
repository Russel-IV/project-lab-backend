package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.repositories.AddressRepository
import com.team1.project_lab_backend.inventory.repositories.CityCountryProjection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DestinationServiceTest {
    private val addressRepository = Mockito.mock(AddressRepository::class.java)
    private val destinationService = DestinationService(addressRepository)

    private fun projection(
        city: String,
        countryCode: String,
    ) = object : CityCountryProjection {
        override val city: String = city
        override val countryCode: String = countryCode
    }

    @Test
    fun getAllDestinationsMapsProjectionsToDestinations() {
        Mockito.`when`(addressRepository.findDistinctCityCountryPairs()).thenReturn(
            listOf(projection("Paris", "FR"), projection("Miami", "US")),
        )

        val result = destinationService.getAllDestinations()

        assertEquals(2, result.size)
        assertEquals("Paris", result[0].city)
        assertEquals("FR", result[0].countryCode)
        assertEquals("Miami", result[1].city)
        assertEquals("US", result[1].countryCode)
    }

    @Test
    fun getAllDestinationsReturnsEmptyListWhenNoAddressesExist() {
        Mockito.`when`(addressRepository.findDistinctCityCountryPairs()).thenReturn(emptyList())

        val result = destinationService.getAllDestinations()

        assertEquals(emptyList<Any>(), result)
    }
}
