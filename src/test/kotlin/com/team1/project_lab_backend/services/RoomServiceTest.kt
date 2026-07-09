package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.PropertyType
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.models.Stay
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

    private fun sampleStay(stayId: Int = 10, hostId: Int = 1) = Stay(
        id = stayId,
        name = "Test Stay",
        propertyType = PropertyType.HOME,
        host = Host(id = hostId),
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    private fun stubStay(stayId: Int = 10, hostId: Int = 1) {
        Mockito.`when`(stayRepository.findById(stayId)).thenReturn(Optional.of(sampleStay(stayId, hostId)))
    }

    // ---- createRoom ----

    @Test
    fun createRoomReturnsPersistedRoom() {
        stubStay()
        val saved = savedRoom()
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenReturn(saved)

        val result = roomService.createRoom(10, baseRequest(), 1)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
        assertEquals("Deluxe Suite", result.name)
    }

    @Test
    fun createRoomRejectsUnknownStay() {
        Mockito.`when`(stayRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(99, baseRequest(), 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun createRoomRejectsBlankName() {
        stubStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(name = "  "), 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNegativePrice() {
        stubStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(price = BigDecimal("-1.00")), 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsZeroSleeps() {
        stubStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(sleeps = 0), 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createRoomRejectsNegativeBathrooms() {
        stubStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.createRoom(10, baseRequest().copy(bathrooms = BigDecimal("-0.5")), 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updateRoom ----

    @Test
    fun updateRoomReturnsUpdatedRoom() {
        val existing = savedRoom(id = 1, stayId = 10)
        Mockito.`when`(roomRepository.findById(1)).thenReturn(Optional.of(existing))
        stubStay()
        val updated = savedRoom(id = 1, stayId = 10)
        Mockito.`when`(roomRepository.save(Mockito.any(Room::class.java))).thenReturn(updated)

        val result = roomService.updateRoom(1, baseRequest(), 1)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
    }

    @Test
    fun updateRoomReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.updateRoom(99, baseRequest(), 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteRoom ----

    @Test
    fun deleteRoomReturnsNotFoundWhenMissing() {
        Mockito.`when`(roomRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.deleteRoom(99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteRoomInvokesRepository() {
        val room = savedRoom(id = 1, stayId = 10)
        Mockito.`when`(roomRepository.findById(1)).thenReturn(Optional.of(room))
        stubStay()

        roomService.deleteRoom(1, 1)

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
                eqArg(10), eqArg(checkIn), eqArg(checkOut), anyArg(), eqArg(null)
            )
        ).thenReturn(rooms)

        val result = roomService.getAvailableRooms(10, checkIn, checkOut)

        assertEquals(1, result.size)
    }

    @Test
    fun getAvailableRoomsRejectsNonPositiveGuests() {
        val checkIn = LocalDate.now().plusDays(1)
        val checkOut = LocalDate.now().plusDays(3)

        val ex = assertThrows(ResponseStatusException::class.java) {
            roomService.getAvailableRooms(10, checkIn, checkOut, guests = 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getAvailableRoomsPassesGuestsToRepository() {
        val checkIn = LocalDate.now().plusDays(1)
        val checkOut = LocalDate.now().plusDays(3)
        val rooms = listOf(savedRoom())
        Mockito.`when`(
            roomRepository.findAvailableRooms(
                eqArg(10), eqArg(checkIn), eqArg(checkOut), anyArg(), eqArg(4)
            )
        ).thenReturn(rooms)

        val result = roomService.getAvailableRooms(10, checkIn, checkOut, guests = 4)

        assertEquals(1, result.size)
        Mockito.verify(roomRepository).findAvailableRooms(
            eqArg(10), eqArg(checkIn), eqArg(checkOut), anyArg(), eqArg(4)
        )
    }
}
