package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.models.Booking
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.services.BookingService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
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
    private val authenticatedUser = User(id = 1, name = "Alice")

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun authenticateAs(user: User) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, emptyList())
    }

    private fun sampleBooking(id: Int = 1) = Booking(
        id = id,
        user = authenticatedUser,
        checkInDate = tomorrow,
        checkOutDate = dayAfter,
        status = BookingStatus.PENDING,
        guestsCount = 2,
        createdAt = LocalDateTime.now(),
    )

    // ---- queries ----

    @Test
    fun bookingsDelegatesToService() {
        val page = listOf(sampleBooking(1), sampleBooking(2))
        Mockito.`when`(bookingService.getAllBookings(0, 2)).thenReturn(page)

        val result = resolver.bookings(page = 0, size = 2)

        assertEquals(listOf(1, 2), result.map { it.id })
        Mockito.verify(bookingService).getAllBookings(0, 2)
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
        authenticateAs(authenticatedUser)
        val saved = sampleBooking(10)
        Mockito.`when`(bookingService.createBooking(anyArg())).thenReturn(saved)

        val input = CreateBookingInput(
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
    fun createBookingRequiresAuthentication() {
        val input = CreateBookingInput(
            checkInDate = tomorrow,
            checkOutDate = dayAfter,
            guestsCount = 2,
            roomIds = setOf(5),
        )
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.createBooking(input) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun updateBookingStatusDelegatesToService() {
        authenticateAs(authenticatedUser)
        val confirmed = Booking(
            id = 1, user = authenticatedUser,
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
        authenticateAs(authenticatedUser)
        Mockito.doNothing().`when`(bookingService).deleteBooking(1)

        val result = resolver.deleteBooking(1)

        assertEquals(true, result)
        Mockito.verify(bookingService).deleteBooking(1)
    }
}
