package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AddressRequest
import com.team1.project_lab_backend.inventory.dto.StayConnection
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

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
            address = Address(id = 1, streetAddress = "123 Main", city = "Testville", countryCode = "US", regionId = 1),
        )

    // ---- searchStays ----

    @Test
    fun searchStaysDelegatesToFeignClient() =
        runTest {
            val filter = StayFilter()
            val connection = StayConnection(items = listOf(sampleStay()), totalCount = 1, hasNextPage = false)
            Mockito.`when`(stayFeignClient.search(filter, 0, 20)).thenReturn(connection)

            val result = stayService.searchStays(filter)

            assertEquals(1, result.items.size)
            assertEquals(1, result.totalCount)
            assertEquals(false, result.hasNextPage)
        }

    @Test
    fun searchStaysMapsFeignBadRequest() =
        runTest {
            val filter = StayFilter(guests = -1)
            Mockito.`when`(stayFeignClient.search(filter, 0, 20))
                .thenThrow(webClientException(400, """{"message":"guests must be at least 1"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.searchStays(filter) }

            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("guests must be at least 1", ex.reason)
        }

    // ---- getStayById ----

    @Test
    fun getStayByIdReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(stayFeignClient.get(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.getStayById(99) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun getStayByIdDelegatesToFeignClient() =
        runTest {
            Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay())

            val result = stayService.getStayById(10)

            assertEquals(10, result.id)
        }

    // ---- createStay ----

    @Test
    fun createStayReturnsPersistedStay() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(stayFeignClient.create(request, 1)).thenReturn(sampleStay())

            val result = stayService.createStay(request, 1)

            assertEquals(10, result.id)
        }

    @Test
    fun createStayRejectsNonOwner() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(stayFeignClient.create(request, 2)).thenThrow(webClientException(403))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.createStay(request, 2) }

            assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        }

    @Test
    fun createStayMapsFeignBadRequestWithMessage() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(stayFeignClient.create(request, 1)).thenThrow(webClientException(400, """{"message":"hostId not found"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.createStay(request, 1) }

            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("hostId not found", ex.reason)
        }

    // ---- updateStay ----

    @Test
    fun updateStayReturnsNotFoundWhenMissing() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(stayFeignClient.update(55, request, 1)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.updateStay(55, request, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun updateStayReturnsUpdatedStay() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(stayFeignClient.update(20, request, 1)).thenReturn(sampleStay(id = 20))

            val result = stayService.updateStay(20, request, 1)

            assertEquals(20, result.id)
        }

    // ---- deleteStay ----

    @Test
    fun deleteStayReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.doThrow(webClientException(404)).`when`(stayFeignClient).delete(99, 1)

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.deleteStay(99, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun deleteStayInvokesFeignClient() =
        runTest {
            stayService.deleteStay(10, 1)

            Mockito.verify(stayFeignClient).delete(10, 1)
        }

    // ---- requireOwnedByHost ----

    @Test
    fun requireOwnedByHostReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(stayFeignClient.get(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.requireOwnedByHost(99, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun requireOwnedByHostRejectsNonOwner() =
        runTest {
            Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay(id = 10))

            val ex = assertThrowsSuspend<ResponseStatusException> { stayService.requireOwnedByHost(10, 2) }

            assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        }

    @Test
    fun requireOwnedByHostReturnsStayForOwner() =
        runTest {
            Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay(id = 10))

            val result = stayService.requireOwnedByHost(10, 1)

            assertEquals(10, result.id)
        }
}
