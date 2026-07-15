package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.models.AmenityType
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

class AmenityServiceTest {
    private val amenityFeignClient = Mockito.mock(AmenityFeignClient::class.java)
    private val amenityService = AmenityService(amenityFeignClient)

    private fun amenity(id: Int = 1) = Amenity(id = id, name = "Wi-Fi", type = AmenityType.ROOM_AMENITY)

    private fun feignBadRequest(body: String) = FeignException.BadRequest(
        "bad request", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    // ---- createAmenity ----

    @Test
    fun createAmenityReturnsPersistedAmenity() {
        val request = AmenityRequest(name = "Wi-Fi", type = AmenityType.ROOM_AMENITY)
        Mockito.`when`(amenityFeignClient.create(request)).thenReturn(amenity())

        val result = amenityService.createAmenity(request)

        assertEquals(1, result.id)
        assertEquals("Wi-Fi", result.name)
        assertEquals(AmenityType.ROOM_AMENITY, result.type)
    }

    @Test
    fun createAmenityMapsFeignBadRequestWithMessage() {
        val request = AmenityRequest(name = "  ", type = AmenityType.PROPERTY_AMENITY)
        Mockito.`when`(amenityFeignClient.create(request)).thenThrow(feignBadRequest("""{"message":"name must not be blank"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { amenityService.createAmenity(request) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("name must not be blank", ex.reason)
    }

    // ---- getAmenityById ----

    @Test
    fun getAmenityByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(amenityFeignClient.get(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { amenityService.getAmenityById(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- updateAmenity ----

    @Test
    fun updateAmenityReturnsNotFoundWhenMissing() {
        val request = AmenityRequest(name = "Pool", type = AmenityType.PROPERTY_AMENITY)
        Mockito.`when`(amenityFeignClient.update(99, request)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { amenityService.updateAmenity(99, request) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateAmenityReturnsUpdatedAmenity() {
        val request = AmenityRequest(name = "Pool", type = AmenityType.PROPERTY_AMENITY)
        Mockito.`when`(amenityFeignClient.update(1, request)).thenReturn(Amenity(id = 1, name = "Pool", type = AmenityType.PROPERTY_AMENITY))

        val result = amenityService.updateAmenity(1, request)

        assertEquals("Pool", result.name)
        assertEquals(AmenityType.PROPERTY_AMENITY, result.type)
    }

    // ---- deleteAmenity ----

    @Test
    fun deleteAmenityReturnsNotFoundWhenMissing() {
        Mockito.`when`(amenityFeignClient.delete(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { amenityService.deleteAmenity(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteAmenityInvokesFeignClient() {
        amenityService.deleteAmenity(1)

        Mockito.verify(amenityFeignClient).delete(1)
    }
}
