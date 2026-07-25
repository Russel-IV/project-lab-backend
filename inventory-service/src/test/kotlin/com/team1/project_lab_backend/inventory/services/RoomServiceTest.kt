package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.models.AmenityType
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Region
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.AmenityRepository
import com.team1.project_lab_backend.inventory.repositories.RoomRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class RoomServiceTest {
    private val roomRepository = Mockito.mock(RoomRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)
    private val amenityRepository = Mockito.mock(AmenityRepository::class.java)
    private val bookingAvailabilityClient = Mockito.mock(BookingAvailabilityClient::class.java)
    private val roomService = RoomService(roomRepository, stayRepository, amenityRepository, bookingAvailabilityClient)

    private fun sampleStay(
        id: Int = 10,
        hostId: Int = 1,
    ) = Stay(
        id = id,
        publicId = UUID.randomUUID(),
        name = "Test Stay",
        propertyType = PropertyType.HOME,
        hostId = hostId,
        address =
            Address(
                id = 1,
                streetAddress = "1 Main St",
                city = "Springfield",
                countryCode = "US",
                region = Region(id = 1, city = "Springfield", countryCode = "US"),
            ),
    )

    private fun baseRequest(amenityIds: Set<Int> = emptySet()) =
        RoomRequest(
            name = "Deluxe Suite",
            price = BigDecimal("150.00"),
            sleeps = 2,
            bedroomAmount = 1,
            bathrooms = BigDecimal("1.0"),
            amenityIds = amenityIds,
        )

    private fun roomAmenity(
        id: Int = 1,
        type: AmenityType = AmenityType.ROOM_AMENITY,
    ) = Amenity(id = id, name = "Air Conditioning", type = type)

    // ---- createRoom ----

    @Test
    fun createRoomPersistsAssignedAmenities() {
        val stay = sampleStay()
        Mockito.`when`(stayRepository.findById(10)).thenReturn(Optional.of(stay))
        val amenity = roomAmenity()
        Mockito.`when`(amenityRepository.findAllById(setOf(1))).thenReturn(listOf(amenity))
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenAnswer { it.arguments[0] }

        val result = roomService.createRoom(10, baseRequest(amenityIds = setOf(1)), 1)

        assertEquals(setOf(1), result.amenityIds)
    }

    @Test
    fun createRoomRejectsUnknownAmenityId() {
        Mockito.`when`(stayRepository.findById(10)).thenReturn(Optional.of(sampleStay()))
        Mockito.`when`(amenityRepository.findAllById(setOf(99))).thenReturn(emptyList())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                roomService.createRoom(10, baseRequest(amenityIds = setOf(99)), 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsPropertyLevelAmenity() {
        Mockito.`when`(stayRepository.findById(10)).thenReturn(Optional.of(sampleStay()))
        val propertyAmenity = roomAmenity(id = 2, type = AmenityType.PROPERTY_AMENITY)
        Mockito.`when`(amenityRepository.findAllById(setOf(2))).thenReturn(listOf(propertyAmenity))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                roomService.createRoom(10, baseRequest(amenityIds = setOf(2)), 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNonOwner() {
        Mockito.`when`(stayRepository.findById(10)).thenReturn(Optional.of(sampleStay(hostId = 1)))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                roomService.createRoom(10, baseRequest(), 2)
            }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    // ---- updateRoom ----

    @Test
    fun updateRoomReplacesAmenities() {
        val existing =
            Room(
                id = 1,
                stayId = 10,
                name = "Deluxe Suite",
                price = BigDecimal("150.00"),
                sleeps = 2,
                bedroomAmount = 1,
                bathrooms = BigDecimal("1.0"),
                amenities = mutableSetOf(roomAmenity(id = 1)),
            )
        Mockito.`when`(roomRepository.findById(1)).thenReturn(Optional.of(existing))
        Mockito.`when`(stayRepository.findById(10)).thenReturn(Optional.of(sampleStay()))
        val newAmenity = roomAmenity(id = 2)
        Mockito.`when`(amenityRepository.findAllById(setOf(2))).thenReturn(listOf(newAmenity))
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenAnswer { it.arguments[0] }

        val result = roomService.updateRoom(1, baseRequest(amenityIds = setOf(2)), 1)

        assertEquals(setOf(2), result.amenityIds)
    }

    // ---- getRoomById ----

    @Test
    fun getRoomByIdExposesAmenityIds() {
        val room =
            Room(
                id = 1,
                stayId = 10,
                name = "Deluxe Suite",
                price = BigDecimal("150.00"),
                sleeps = 2,
                bedroomAmount = 1,
                bathrooms = BigDecimal("1.0"),
                amenities = mutableSetOf(roomAmenity(id = 1)),
            )
        Mockito.`when`(roomRepository.findById(1)).thenReturn(Optional.of(room))

        val result = roomService.getRoomById(1)

        assertEquals(setOf(1), result.amenityIds)
    }
}
