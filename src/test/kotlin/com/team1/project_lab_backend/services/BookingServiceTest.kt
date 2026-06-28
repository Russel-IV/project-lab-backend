package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.BookingRequest
import com.team1.project_lab_backend.dto.BookingStatusRequest
import com.team1.project_lab_backend.models.Booking
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.BookingRepository
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }

class BookingServiceTest {

    private val bookingRepository = Mockito.mock(BookingRepository::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val roomRepository = Mockito.mock(RoomRepository::class.java)

    private val bookingService = BookingService(bookingRepository, userRepository, roomRepository)

    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)
    private val dayAfterTomorrow: LocalDate = LocalDate.now().plusDays(2)

    private fun room(id: Int, stayId: Int = 1, sleeps: Int = 2): Room =
        Room(id = id, stayId = stayId, name = "Room $id", price = BigDecimal("100.00"),
            sleeps = sleeps, bedroomAmount = 1, bathrooms = BigDecimal("1.0"))

    private fun user(id: Int = 1): User = User(id = id, name = "Alice")

    private fun baseRequest(
        userId: Int = 1,
        roomIds: Set<Int> = setOf(10),
        checkIn: LocalDate = tomorrow,
        checkOut: LocalDate = dayAfterTomorrow,
        guests: Int = 1,
    ) = BookingRequest(
        userId = userId,
        checkInDate = checkIn,
        checkOutDate = checkOut,
        guestsCount = guests,
        roomIds = roomIds,
    )

    private fun stubHappyPath(roomIds: Set<Int> = setOf(10), sleeps: Int = 2): Booking {
        val u = user()
        val rooms = roomIds.map { room(it, sleeps = sleeps) }
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(u))
        Mockito.`when`(roomRepository.findAllById(roomIds)).thenReturn(rooms)
        Mockito.`when`(
            roomRepository.findConflictingRooms(
                anyArg(), anyArg(), anyArg(), anyArg()
            )
        ).thenReturn(emptyList())
        val saved = Booking(
            id = 99, user = u, checkInDate = tomorrow, checkOutDate = dayAfterTomorrow,
            status = BookingStatus.PENDING, guestsCount = 1, rooms = rooms.toMutableSet()
        )
        Mockito.`when`(bookingRepository.save(Mockito.any(Booking::class.java))).thenReturn(saved)
        return saved
    }

    // ---- createBooking ----

    @Test
    fun createBookingReturnsPersistedBooking() {
        stubHappyPath()

        val result = bookingService.createBooking(baseRequest())

        assertEquals(99, result.id)
        assertEquals(BookingStatus.PENDING, result.status)
        assertEquals(tomorrow, result.checkInDate)
    }

    @Test
    fun createBookingRejectsCheckInInPast() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(checkIn = LocalDate.now().minusDays(1)))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsCheckInMoreThanSixMonthsAway() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(
                baseRequest(checkIn = LocalDate.now().plusMonths(7))
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsCheckOutNotAfterCheckIn() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(checkIn = tomorrow, checkOut = tomorrow))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsEmptyRoomIds() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(roomIds = emptySet()))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsZeroGuestsCount() {
        // Needs userId stub to get past the user lookup
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(user()))
        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(guests = 0))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsUnknownUserId() {
        Mockito.`when`(userRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(userId = 99))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsUnknownRoomIds() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(user()))
        Mockito.`when`(roomRepository.findAllById(setOf(10))).thenReturn(emptyList())

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest())
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsRoomsFromDifferentStays() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(user()))
        val mixedStayRooms = listOf(room(10, stayId = 1), room(11, stayId = 2))
        Mockito.`when`(roomRepository.findAllById(setOf(10, 11))).thenReturn(mixedStayRooms)

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(roomIds = setOf(10, 11)))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsConflictingRooms() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(user()))
        val rooms = listOf(room(10))
        Mockito.`when`(roomRepository.findAllById(setOf(10))).thenReturn(rooms)
        Mockito.`when`(
            roomRepository.findConflictingRooms(
                anyArg(), anyArg(), anyArg(), anyArg()
            )
        ).thenReturn(rooms)

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest())
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsGuestsExceedingCapacity() {
        // Room sleeps 1, requesting 5 guests
        stubHappyPath(sleeps = 1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.createBooking(baseRequest(guests = 5))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updateBookingStatus ----

    @Test
    fun updateBookingStatusReturnsUpdatedBooking() {
        val u = user()
        val existing = Booking(
            id = 5, user = u, checkInDate = tomorrow, checkOutDate = dayAfterTomorrow,
            status = BookingStatus.PENDING, guestsCount = 1,
            createdAt = LocalDateTime.now(), rooms = mutableSetOf()
        )
        Mockito.`when`(bookingRepository.findById(5)).thenReturn(Optional.of(existing))
        val updated = existing.copy(status = BookingStatus.CONFIRMED)
        Mockito.`when`(bookingRepository.save(Mockito.any(Booking::class.java))).thenReturn(updated)

        val result = bookingService.updateBookingStatus(5, BookingStatusRequest(BookingStatus.CONFIRMED))

        assertEquals(BookingStatus.CONFIRMED, result.status)
    }

    @Test
    fun updateBookingStatusReturnsNotFoundWhenMissing() {
        Mockito.`when`(bookingRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.updateBookingStatus(99, BookingStatusRequest(BookingStatus.CANCELLED))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteBooking ----

    @Test
    fun deleteBookingReturnsNotFoundWhenMissing() {
        Mockito.`when`(bookingRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            bookingService.deleteBooking(99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteBookingInvokesRepository() {
        val u = user()
        val booking = Booking(
            id = 5, user = u, checkInDate = tomorrow, checkOutDate = dayAfterTomorrow,
            status = BookingStatus.PENDING, guestsCount = 1, rooms = mutableSetOf()
        )
        Mockito.`when`(bookingRepository.findById(5)).thenReturn(Optional.of(booking))

        bookingService.deleteBooking(5, 1)

        Mockito.verify(bookingRepository).deleteById(5)
    }
}

// Booking is a data class — copy is available
private fun Booking.copy(status: BookingStatus) = Booking(
    id = id, user = user, checkInDate = checkInDate, checkOutDate = checkOutDate,
    status = status, guestsCount = guestsCount, createdAt = createdAt, rooms = rooms
)
