package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AddressRequest
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets

class StayServiceTest {
    private val stayFeignClient = Mockito.mock(StayFeignClient::class.java)
    private val stayService = StayService(stayFeignClient)

    private fun baseRequest() =
        StayRequest(
            name = "Test Stay",
            propertyType = PropertyType.HOME,
            address = AddressRequest(streetAddress = "123 Main", city = "Testville", countryCode = "US"),
            hostId = 1,
        )

    private fun sampleStay(id: Int = 10) =
        Stay(
            id = id,
            name = "Test Stay",
            propertyType = PropertyType.HOME,
            hostId = 1,
            address = Address(id = 1, streetAddress = "123 Main", city = "Testville", countryCode = "US"),
        )

    private fun feignBadRequest(body: String) =
        FeignException.BadRequest(
            "bad request",
            Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
            body.toByteArray(StandardCharsets.UTF_8),
            emptyMap(),
        )

    // ---- searchStays ----

    @Test
    fun searchStaysDelegatesToFeignClient() {
        val filter = StayFilter()
        Mockito.`when`(stayFeignClient.search(filter, 0, 20)).thenReturn(listOf(sampleStay()))

        val result = stayService.searchStays(filter)

        assertEquals(1, result.size)
    }

    @Test
    fun searchStaysMapsFeignBadRequest() {
        val filter = StayFilter(guests = -1)
        Mockito.`when`(stayFeignClient.search(filter, 0, 20)).thenThrow(feignBadRequest("""{"message":"guests must be at least 1"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.searchStays(filter) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("guests must be at least 1", ex.reason)
    }

    // ---- getStayById ----

    @Test
    fun getStayByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(stayFeignClient.get(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.getStayById(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getStayByIdDelegatesToFeignClient() {
        Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay())

        val result = stayService.getStayById(10)

        assertEquals(10, result.id)
    }

    // ---- createStay ----

    @Test
    fun createStayReturnsPersistedStay() {
        val request = baseRequest()
        Mockito.`when`(stayFeignClient.create(request, 1)).thenReturn(sampleStay())

        val result = stayService.createStay(request, 1)

        assertEquals(10, result.id)
    }

    @Test
    fun createStayRejectsNonOwner() {
        val request = baseRequest()
        Mockito.`when`(stayFeignClient.create(request, 2)).thenThrow(FeignException.Forbidden::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.createStay(request, 2) }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun createStayMapsFeignBadRequestWithMessage() {
        val request = baseRequest()
        Mockito.`when`(stayFeignClient.create(request, 1)).thenThrow(feignBadRequest("""{"message":"hostId not found"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.createStay(request, 1) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("hostId not found", ex.reason)
    }

    // ---- updateStay ----

    @Test
    fun updateStayReturnsNotFoundWhenMissing() {
        val request = baseRequest()
        Mockito.`when`(stayFeignClient.update(55, request, 1)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.updateStay(55, request, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateStayReturnsUpdatedStay() {
        val request = baseRequest()
        Mockito.`when`(stayFeignClient.update(20, request, 1)).thenReturn(sampleStay(id = 20))

        val result = stayService.updateStay(20, request, 1)

        assertEquals(20, result.id)
    }

    // ---- deleteStay ----

    @Test
    fun deleteStayReturnsNotFoundWhenMissing() {
        Mockito.doThrow(FeignException.NotFound::class.java).`when`(stayFeignClient).delete(99, 1)

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.deleteStay(99, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteStayInvokesFeignClient() {
        stayService.deleteStay(10, 1)

        Mockito.verify(stayFeignClient).delete(10, 1)
    }
}
