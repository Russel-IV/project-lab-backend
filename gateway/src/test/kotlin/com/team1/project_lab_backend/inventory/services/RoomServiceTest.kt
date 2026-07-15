package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class RoomServiceTest {
    private val roomFeignClient = Mockito.mock(RoomFeignClient::class.java)
    private val roomService = RoomService(roomFeignClient)

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

    private fun feignBadRequest(body: String) = FeignException.BadRequest(
        "bad request", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    // ---- getRoomsForStay ----

    @Test
    fun getRoomsForStayReturnsNotFoundWhenStayMissing() {
        Mockito.`when`(roomFeignClient.list(null, 99, null, 0, 20)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.getRoomsForStay(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getRoomsForStayDelegatesToFeignClient() {
        Mockito.`when`(roomFeignClient.list(null, 10, null, 0, 20)).thenReturn(listOf(savedRoom()))

        val result = roomService.getRoomsForStay(10)

        assertEquals(1, result.size)
    }

    // ---- getRoomById ----

    @Test
    fun getRoomByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomFeignClient.get(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.getRoomById(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- createRoom ----

    @Test
    fun createRoomReturnsPersistedRoom() {
        val request = baseRequest()
        Mockito.`when`(roomFeignClient.create(10, request, 1)).thenReturn(savedRoom())

        val result = roomService.createRoom(10, request, 1)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
        assertEquals("Deluxe Suite", result.name)
    }

    @Test
    fun createRoomRejectsUnknownStay() {
        val request = baseRequest()
        Mockito.`when`(roomFeignClient.create(99, request, 1)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.createRoom(99, request, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNonOwner() {
        val request = baseRequest()
        Mockito.`when`(roomFeignClient.create(10, request, 2)).thenThrow(FeignException.Forbidden::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.createRoom(10, request, 2) }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun createRoomMapsFeignBadRequestWithMessage() {
        val request = baseRequest().copy(price = BigDecimal("-1.00"))
        Mockito.`when`(roomFeignClient.create(10, request, 1)).thenThrow(feignBadRequest("""{"message":"price must be >= 0"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.createRoom(10, request, 1) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("price must be >= 0", ex.reason)
    }

    // ---- updateRoom ----

    @Test
    fun updateRoomReturnsUpdatedRoom() {
        val request = baseRequest()
        Mockito.`when`(roomFeignClient.update(1, request, 1)).thenReturn(savedRoom(id = 1, stayId = 10))

        val result = roomService.updateRoom(1, request, 1)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
    }

    @Test
    fun updateRoomReturnsNotFoundWhenMissing() {
        val request = baseRequest()
        Mockito.`when`(roomFeignClient.update(99, request, 1)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.updateRoom(99, request, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteRoom ----

    @Test
    fun deleteRoomReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomFeignClient.delete(99, 1)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.deleteRoom(99, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteRoomInvokesFeignClient() {
        roomService.deleteRoom(1, 1)

        Mockito.verify(roomFeignClient).delete(1, 1)
    }

    // ---- getAvailableRooms ----

    @Test
    fun getAvailableRoomsDelegatesToFeignClient() {
        val checkIn = LocalDate.now().plusDays(1)
        val checkOut = LocalDate.now().plusDays(3)
        Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, null)).thenReturn(listOf(savedRoom()))

        val result = roomService.getAvailableRooms(10, checkIn, checkOut)

        assertEquals(1, result.size)
    }

    @Test
    fun getAvailableRoomsPassesGuestsToFeignClient() {
        val checkIn = LocalDate.now().plusDays(1)
        val checkOut = LocalDate.now().plusDays(3)
        Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, 4)).thenReturn(listOf(savedRoom()))

        val result = roomService.getAvailableRooms(10, checkIn, checkOut, guests = 4)

        assertEquals(1, result.size)
        Mockito.verify(roomFeignClient).available(10, checkIn, checkOut, 4)
    }

    @Test
    fun getAvailableRoomsMapsFeignBadRequest() {
        val checkIn = LocalDate.now().plusDays(3)
        val checkOut = LocalDate.now().plusDays(1)
        Mockito.`when`(roomFeignClient.available(10, checkIn, checkOut, null))
            .thenThrow(feignBadRequest("""{"message":"checkOut must be after checkIn"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { roomService.getAvailableRooms(10, checkIn, checkOut) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("checkOut must be after checkIn", ex.reason)
    }
}
