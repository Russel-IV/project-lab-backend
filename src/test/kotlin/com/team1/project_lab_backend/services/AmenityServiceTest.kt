package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AmenityRequest
import com.team1.project_lab_backend.models.Amenity
import com.team1.project_lab_backend.models.AmenityType
import com.team1.project_lab_backend.repositories.AmenityRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class AmenityServiceTest {

    private val amenityRepository = Mockito.mock(AmenityRepository::class.java)
    private val amenityService = AmenityService(amenityRepository)

    private fun amenity(id: Int = 1) = Amenity(id = id, name = "Wi-Fi", type = AmenityType.ROOM_AMENITY)

    @Test
    fun createAmenityReturnsPersistedAmenity() {
        val saved = amenity()
        Mockito.`when`(amenityRepository.save(Mockito.any(Amenity::class.java))).thenReturn(saved)

        val result = amenityService.createAmenity(AmenityRequest(name = "Wi-Fi", type = AmenityType.ROOM_AMENITY))

        assertEquals(1, result.id)
        assertEquals("Wi-Fi", result.name)
        assertEquals(AmenityType.ROOM_AMENITY, result.type)
    }

    @Test
    fun createAmenityRejectsBlankName() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            amenityService.createAmenity(AmenityRequest(name = "  ", type = AmenityType.PROPERTY_AMENITY))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getAmenityByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(amenityRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            amenityService.getAmenityById(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateAmenityReturnsNotFoundWhenMissing() {
        Mockito.`when`(amenityRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            amenityService.updateAmenity(99, AmenityRequest(name = "Pool", type = AmenityType.PROPERTY_AMENITY))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateAmenityReturnsUpdatedAmenity() {
        Mockito.`when`(amenityRepository.existsById(1)).thenReturn(true)
        val updated = Amenity(id = 1, name = "Pool", type = AmenityType.PROPERTY_AMENITY)
        Mockito.`when`(amenityRepository.save(Mockito.any(Amenity::class.java))).thenReturn(updated)

        val result = amenityService.updateAmenity(1, AmenityRequest(name = "Pool", type = AmenityType.PROPERTY_AMENITY))

        assertEquals("Pool", result.name)
        assertEquals(AmenityType.PROPERTY_AMENITY, result.type)
    }

    @Test
    fun deleteAmenityReturnsNotFoundWhenMissing() {
        Mockito.`when`(amenityRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            amenityService.deleteAmenity(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteAmenityInvokesRepository() {
        Mockito.`when`(amenityRepository.existsById(1)).thenReturn(true)

        amenityService.deleteAmenity(1)

        Mockito.verify(amenityRepository).deleteById(1)
    }
}
