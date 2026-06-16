package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.models.Booking
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.services.BookingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }
private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class BookingResolverTest {

    private val bookingService = Mockito.mock(BookingService::class.java)
    private val resolver = BookingResolver(bookingService)

    private val tomorrow = LocalDate.now().plusDays(1)
    private val dayAfter = LocalDate.now().plusDays(2)

    private fun sampleBooking(id: Int = 1) = Booking(
        id = id,
        user = User(id = 1, name = "Alice"),
        checkInDate = tomorrow,
        checkOutDate = dayAfter,
        status = BookingStatus.PENDING,
        guestsCount = 2,
        createdAt = LocalDateTime.now(),
    )

    // ---- queries ----

    @Test
    fun bookingsPaginatesInMemory() {
        val all = (1..5).map { sampleBooking(it) }
        Mockito.`when`(bookingService.getAllBookings()).thenReturn(all)

        val page1 = resolver.bookings(page = 0, size = 2)
        val page2 = resolver.bookings(page = 1, size = 2)
        val page3 = resolver.bookings(page = 2, size = 2)

        assertEquals(listOf(1, 2), page1.map { it.id })
        assertEquals(listOf(3, 4), page2.map { it.id })
        assertEquals(listOf(5), page3.map { it.id })
    }

    @Test
    fun bookingByIdDelegatesToService() {
        Mockito.`when`(bookingService.getBookingById(3)).thenReturn(sampleBooking(3))

        val result = resolver.booking(3)

        assertEquals(3, result?.id)
        assertEquals(BookingStatus.PENDING, result?.status)
    }

    @Test
    fun bookingByIdPropagatesNotFoundException() {
        Mockito.`when`(bookingService.getBookingById(99)).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found")
        )

        assertThrows(ResponseStatusException::class.java) { resolver.booking(99) }
    }

    // ---- mutations ----

    @Test
    fun createBookingDelegatesToService() {
        val saved = sampleBooking(10)
        Mockito.`when`(bookingService.createBooking(anyArg())).thenReturn(saved)

        val input = CreateBookingInput(
            userId = 1,
            checkInDate = tomorrow,
            checkOutDate = dayAfter,
            guestsCount = 2,
            roomIds = setOf(5),
        )
        val result = resolver.createBooking(input)

        assertEquals(10, result.id)
        assertEquals(BookingStatus.PENDING, result.status)
        Mockito.verify(bookingService).createBooking(anyArg())
    }

    @Test
    fun updateBookingStatusDelegatesToService() {
        val confirmed = Booking(
            id = 1, user = User(id = 1, name = "Alice"),
            checkInDate = tomorrow, checkOutDate = dayAfter,
            status = BookingStatus.CONFIRMED, guestsCount = 2,
            createdAt = LocalDateTime.now(),
        )
        Mockito.`when`(bookingService.updateBookingStatus(eqArg(1), anyArg())).thenReturn(confirmed)

        val result = resolver.updateBookingStatus(1, BookingStatus.CONFIRMED)

        assertEquals(BookingStatus.CONFIRMED, result.status)
        Mockito.verify(bookingService).updateBookingStatus(eqArg(1), anyArg())
    }

    @Test
    fun deleteBookingReturnsTrueOnSuccess() {
        Mockito.doNothing().`when`(bookingService).deleteBooking(1)

        val result = resolver.deleteBooking(1)

        assertEquals(true, result)
        Mockito.verify(bookingService).deleteBooking(1)
    }
}
