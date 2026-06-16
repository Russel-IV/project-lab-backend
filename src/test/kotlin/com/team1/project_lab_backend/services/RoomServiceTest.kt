package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }
private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class RoomServiceTest {

    private val roomRepository = Mockito.mock(RoomRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)

    private val roomService = RoomService(roomRepository, stayRepository)

    private fun baseRequest() = RoomRequest(
        name = "Deluxe Suite",
        price = BigDecimal("150.00"),
        sleeps = 2,
        bedroomAmount = 1,
        bathrooms = BigDecimal("1.0"),
        size = null,
    )

    private fun savedRoom(id: Int = 1, stayId: Int = 10) = Room(
        id = id, stayId = stayId, name = "Deluxe Suite",
        price = BigDecimal("150.00"), sleeps = 2, bedroomAmount = 1,
        bathrooms = BigDecimal("1.0"),
    )

    // ---- createRoom ----

    @Test
    fun createRoomReturnsPersistedRoom() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)
        val saved = savedRoom()
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenReturn(saved)

        val result = roomService.createRoom(10, baseRequest())

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
        assertEquals("Deluxe Suite", result.name)
    }

    @Test
    fun createRoomRejectsUnknownStay() {
        Mockito.`when`(stayRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(99, baseRequest())
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun createRoomRejectsBlankName() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(name = "  "))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNegativePrice() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(price = BigDecimal("-1.00")))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsZeroSleeps() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(sleeps = 0))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNegativeBathrooms() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(bathrooms = BigDecimal("-0.5")))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updateRoom ----

    @Test
    fun updateRoomReturnsUpdatedRoom() {
        val existing = savedRoom(id = 1, stayId = 10)
        Mockito.`when`(roomRepository.findById(1)).thenReturn(Optional.of(existing))
        val updated = savedRoom(id = 1, stayId = 10)
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenReturn(updated)

        val result = roomService.updateRoom(1, baseRequest())

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
    }

    @Test
    fun updateRoomReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.updateRoom(99, baseRequest())
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteRoom ----

    @Test
    fun deleteRoomReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.deleteRoom(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteRoomInvokesRepository() {
        Mockito.`when`(roomRepository.existsById(1)).thenReturn(true)

        roomService.deleteRoom(1)

        Mockito.verify(roomRepository).deleteById(1)
    }

    // ---- getAvailableRooms ----

    @Test
    fun getAvailableRoomsRejectsCheckOutNotAfterCheckIn() {
        val today = LocalDate.now()

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.getAvailableRooms(1, today.plusDays(3), today.plusDays(1))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getAvailableRoomsRejectsEqualDates() {
        val date = LocalDate.now().plusDays(1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.getAvailableRooms(1, date, date)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getAvailableRoomsReturnsRepositoryResult() {
        val checkIn = LocalDate.now().plusDays(1)
        val checkOut = LocalDate.now().plusDays(3)
        val rooms = listOf(savedRoom())
        Mockito.`when`(
            roomRepository.findAvailableRooms(
                eqArg(10), eqArg(checkIn), eqArg(checkOut), anyArg()
            )
        ).thenReturn(rooms)

        val result = roomService.getAvailableRooms(10, checkIn, checkOut)

        assertEquals(1, result.size)
    }
}
