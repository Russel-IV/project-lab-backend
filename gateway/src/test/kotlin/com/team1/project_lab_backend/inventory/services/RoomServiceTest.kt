package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class RoomServiceTest {
    private val roomFeignClient = Mockito.mock(RoomFeignClient::class.java)
    private val stayFeignClient = Mockito.mock(StayFeignClient::class.java)
    private val stayService = StayService(stayFeignClient)
    private val roomService = RoomService(roomFeignClient, stayService)

    private fun baseRequest() =
        RoomRequest(
            name = "Deluxe Suite",
            price = BigDecimal("150.00"),
            sleeps = 2,
            bedroomAmount = 1,
            bathrooms = BigDecimal("1.0"),
            size = null,
        )

    private fun savedRoom(
        id: Int = 1,
        stayId: Int = 10,
    ) = Room(
        id = id,
        stayId = stayId,
        name = "Deluxe Suite",
        price = BigDecimal("150.00"),
        sleeps = 2,
        bedroomAmount = 1,
        bathrooms = BigDecimal("1.0"),
        size = null,
    )

    private fun sampleStay(
        stayId: Int = 10,
        hostId: Int = 1,
    ) = Stay(
        id = stayId,
        publicId = UUID.randomUUID(),
        name = "Test Stay",
        propertyType = PropertyType.HOME,
        hostId = hostId,
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US", regionId = 1),
    )

    // ---- getRoomsForStay ----

    @Test
    fun getRoomsForStayReturnsNotFoundWhenStayMissing() =
        runTest {
            Mockito.`when`(roomFeignClient.list(null, 99, null, 0, 20)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.getRoomsForStay(99) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun getRoomsForStayDelegatesToFeignClient() =
        runTest {
            Mockito.`when`(roomFeignClient.list(null, 10, null, 0, 20)).thenReturn(listOf(savedRoom()))

            val result = roomService.getRoomsForStay(10)

            assertEquals(1, result.size)
        }

    // ---- getRoomById ----

    @Test
    fun getRoomByIdReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(roomFeignClient.get(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.getRoomById(99) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- createRoom ----

    @Test
    fun createRoomReturnsPersistedRoom() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(roomFeignClient.create(10, request, 1)).thenReturn(savedRoom())

            val result = roomService.createRoom(10, request, 1)

            assertEquals(1, result.id)
            assertEquals(10, result.stayId)
            assertEquals("Deluxe Suite", result.name)
        }

    @Test
    fun createRoomRejectsUnknownStay() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(roomFeignClient.create(99, request, 1)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.createRoom(99, request, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun createRoomRejectsNonOwner() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(roomFeignClient.create(10, request, 2)).thenThrow(webClientException(403))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.createRoom(10, request, 2) }

            assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        }

    @Test
    fun createRoomMapsFeignBadRequestWithMessage() =
        runTest {
            val request = baseRequest().copy(price = BigDecimal("-1.00"))
            Mockito.`when`(roomFeignClient.create(10, request, 1)).thenThrow(webClientException(400, """{"message":"price must be >= 0"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.createRoom(10, request, 1) }

            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("price must be >= 0", ex.reason)
        }

    // ---- updateRoom ----

    @Test
    fun updateRoomReturnsUpdatedRoom() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(roomFeignClient.update(1, request, 1)).thenReturn(savedRoom(id = 1, stayId = 10))

            val result = roomService.updateRoom(1, request, 1)

            assertEquals(1, result.id)
            assertEquals(10, result.stayId)
        }

    @Test
    fun updateRoomReturnsNotFoundWhenMissing() =
        runTest {
            val request = baseRequest()
            Mockito.`when`(roomFeignClient.update(99, request, 1)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.updateRoom(99, request, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- deleteRoom ----

    @Test
    fun deleteRoomReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(roomFeignClient.delete(99, 1)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.deleteRoom(99, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun deleteRoomInvokesFeignClient() =
        runTest {
            roomService.deleteRoom(1, 1)

            Mockito.verify(roomFeignClient).delete(1, 1)
        }

    // ---- getAvailableRooms ----

    @Test
    fun getAvailableRoomsDelegatesToFeignClient() =
        runTest {
            val checkIn = LocalDate.now().plusDays(1)
            val checkOut = LocalDate.now().plusDays(3)
            Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, null)).thenReturn(listOf(savedRoom()))

            val result = roomService.getAvailableRooms(10, checkIn, checkOut)

            assertEquals(1, result.size)
        }

    @Test
    fun getAvailableRoomsPassesGuestsToFeignClient() =
        runTest {
            val checkIn = LocalDate.now().plusDays(1)
            val checkOut = LocalDate.now().plusDays(3)
            Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, 4)).thenReturn(listOf(savedRoom()))

            val result = roomService.getAvailableRooms(10, checkIn, checkOut, guests = 4)

            assertEquals(1, result.size)
            Mockito.verify(roomFeignClient).available(10, checkIn, checkOut, 4)
        }

    @Test
    fun getAvailableRoomsMapsFeignBadRequest() =
        runTest {
            val checkIn = LocalDate.now().plusDays(3)
            val checkOut = LocalDate.now().plusDays(1)
            Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, null))
                .thenThrow(webClientException(400, """{"message":"checkOut must be after checkIn"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.getAvailableRooms(10, checkIn, checkOut) }

            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("checkOut must be after checkIn", ex.reason)
        }

    // ---- requireOwnedByHost ----

    @Test
    fun requireOwnedByHostReturnsNotFoundWhenRoomMissing() =
        runTest {
            Mockito.`when`(roomFeignClient.get(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.requireOwnedByHost(99, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun requireOwnedByHostReturnsNotFoundWhenStayMissing() =
        runTest {
            Mockito.`when`(roomFeignClient.get(1)).thenReturn(savedRoom(id = 1, stayId = 10))
            Mockito.`when`(stayFeignClient.get(10)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.requireOwnedByHost(1, 1) }

            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun requireOwnedByHostRejectsNonOwner() =
        runTest {
            Mockito.`when`(roomFeignClient.get(1)).thenReturn(savedRoom(id = 1, stayId = 10))
            Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay(stayId = 10, hostId = 1))

            val ex = assertThrowsSuspend<ResponseStatusException> { roomService.requireOwnedByHost(1, 2) }

            assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        }

    @Test
    fun requireOwnedByHostReturnsRoomForOwner() =
        runTest {
            Mockito.`when`(roomFeignClient.get(1)).thenReturn(savedRoom(id = 1, stayId = 10))
            Mockito.`when`(stayFeignClient.get(10)).thenReturn(sampleStay(stayId = 10, hostId = 1))

            val result = roomService.requireOwnedByHost(1, 1)

            assertEquals(1, result.id)
        }
}
