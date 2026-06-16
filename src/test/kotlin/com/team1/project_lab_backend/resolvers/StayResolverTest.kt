package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.PropertyType
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.services.StayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }
private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class StayResolverTest {

    private val stayService = Mockito.mock(StayService::class.java)
    private val resolver = StayResolver(stayService)

    private fun sampleStay(id: Int = 1) = Stay(
        id = id,
        name = "Cozy Cabin",
        propertyType = PropertyType.HOME,
        host = Host(id = 42),
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    // ---- queries ----

    @Test
    fun staysPassesPageAndSizeToService() {
        val stays = listOf(sampleStay())
        Mockito.`when`(stayService.getAllStays(2, 5)).thenReturn(stays)

        val result = resolver.stays(2, 5)

        assertEquals(1, result.size)
        assertEquals("Cozy Cabin", result[0].name)
        Mockito.verify(stayService).getAllStays(2, 5)
    }

    @Test
    fun staysReturnsEmptyListWhenNoResults() {
        Mockito.`when`(stayService.getAllStays(0, 20)).thenReturn(emptyList())

        val result = resolver.stays(0, 20)

        assertEquals(0, result.size)
    }

    @Test
    fun stayByIdDelegatestoService() {
        Mockito.`when`(stayService.getStayById(7)).thenReturn(sampleStay(7))

        val result = resolver.stay(7)

        assertEquals(7, result?.id)
        assertEquals("Cozy Cabin", result?.name)
    }

    @Test
    fun stayByIdPropagatesNotFoundException() {
        Mockito.`when`(stayService.getStayById(99)).thenThrow(
            org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "stay not found"
            )
        )

        assertThrows(ResponseStatusException::class.java) {
            resolver.stay(99)
        }
    }

    // ---- mutations ----

    @Test
    fun createStayDelegatesToService() {
        val stay = sampleStay(10)
        Mockito.`when`(stayService.createStay(anyArg())).thenReturn(stay)

        val input = CreateStayInput(
            name = "Cozy Cabin",
            propertyType = PropertyType.HOME,
            address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
            hostId = 42,
        )
        val result = resolver.createStay(input)

        assertEquals(10, result.id)
        Mockito.verify(stayService).createStay(anyArg())
    }

    @Test
    fun updateStayDelegatesToService() {
        val stay = sampleStay(5)
        Mockito.`when`(stayService.updateStay(eqArg(5), anyArg())).thenReturn(stay)

        val input = UpdateStayInput(
            name = "Cozy Cabin",
            propertyType = PropertyType.HOME,
            address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
            hostId = 42,
        )
        val result = resolver.updateStay(5, input)

        assertEquals(5, result.id)
        Mockito.verify(stayService).updateStay(eqArg(5), anyArg())
    }

    @Test
    fun deleteStayReturnsTrueOnSuccess() {
        Mockito.doNothing().`when`(stayService).deleteStay(1)

        val result = resolver.deleteStay(1)

        assertEquals(true, result)
        Mockito.verify(stayService).deleteStay(1)
    }
}
