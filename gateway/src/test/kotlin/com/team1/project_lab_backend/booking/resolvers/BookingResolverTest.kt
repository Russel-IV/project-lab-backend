package com.team1.project_lab_backend.booking.resolvers

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.services.BookingService
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.withAuthenticatedUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class BookingResolverTest {
    private val bookingService = Mockito.mock(BookingService::class.java)
    private val resolver = BookingResolver(bookingService)

    private val tomorrow = LocalDate.now().plusDays(1)
    private val dayAfter = LocalDate.now().plusDays(2)
    private val authenticatedUserId = 1

    private fun sampleBooking(id: Int = 1) =
        Booking(
            id = id,
            userId = authenticatedUserId,
            checkInDate = tomorrow,
            checkOutDate = dayAfter,
            status = BookingStatus.PENDING,
            guestsCount = 2,
            createdAt = LocalDateTime.now(),
            totalPrice = BigDecimal("200.00"),
        )

    // ---- queries ----

    @Test
    fun bookingsDelegatesToService() =
        runTest {
            val page = listOf(sampleBooking(1), sampleBooking(2))
            Mockito.`when`(bookingService.getAllBookings(0, 2)).thenReturn(page)

            val result = resolver.bookings(page = 0, size = 2)

            assertEquals(listOf(1, 2), result.map { it.id })
            Mockito.verify(bookingService).getAllBookings(0, 2)
        }

    @Test
    fun bookingByIdDelegatesToService() =
        runTest {
            Mockito.`when`(bookingService.getBookingById(3)).thenReturn(sampleBooking(3))

            val result = resolver.booking(3)

            assertEquals(3, result?.id)
            assertEquals(BookingStatus.PENDING, result?.status)
        }

    @Test
    fun bookingByIdPropagatesNotFoundException() =
        runTest {
            Mockito.`when`(bookingService.getBookingById(99)).thenThrow(
                ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"),
            )

            assertThrowsSuspend<ResponseStatusException> { resolver.booking(99) }
        }

    @Test
    fun myBookingsDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                val page = listOf(sampleBooking(1), sampleBooking(2))
                Mockito.`when`(bookingService.getBookingsByUser(1, 0, 2)).thenReturn(page)

                val result = resolver.myBookings(page = 0, size = 2)

                assertEquals(listOf(1, 2), result.map { it.id })
                Mockito.verify(bookingService).getBookingsByUser(1, 0, 2)
            }
        }

    @Test
    fun myBookingsRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.myBookings(null, null) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    @Test
    fun myBookingStatusForStayDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(bookingService.hasCompletedBookingForStay(1, 2)).thenReturn(true)

                val result = resolver.myBookingStatusForStay(2)

                assertEquals(true, result.hasCompletedBooking)
            }
        }

    @Test
    fun myBookingStatusForStayRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.myBookingStatusForStay(2) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    // ---- mutations ----

    @Test
    fun createBookingDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                val saved = sampleBooking(10)
                Mockito.`when`(bookingService.createBooking(anyArg())).thenReturn(saved)

                val input =
                    CreateBookingInput(
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
        }

    @Test
    fun createBookingRequiresAuthentication() =
        runTest {
            val input =
                CreateBookingInput(
                    checkInDate = tomorrow,
                    checkOutDate = dayAfter,
                    guestsCount = 2,
                    roomIds = setOf(5),
                )
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.createBooking(input) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    @Test
    fun updateBookingStatusDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                val confirmed =
                    Booking(
                        id = 1,
                        userId = authenticatedUserId,
                        checkInDate = tomorrow,
                        checkOutDate = dayAfter,
                        status = BookingStatus.CONFIRMED,
                        guestsCount = 2,
                        createdAt = LocalDateTime.now(),
                        totalPrice = BigDecimal("200.00"),
                    )
                Mockito.`when`(bookingService.updateBookingStatus(eqArg(1), anyArg())).thenReturn(confirmed)

                val result = resolver.updateBookingStatus(1, BookingStatus.CONFIRMED)

                assertEquals(BookingStatus.CONFIRMED, result.status)
                Mockito.verify(bookingService).updateBookingStatus(eqArg(1), anyArg())
            }
        }

    @Test
    fun deleteBookingReturnsTrueOnSuccess() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(bookingService.deleteBooking(eqArg(1), eqArg(1))).thenReturn(Unit)

                val result = resolver.deleteBooking(1)

                assertEquals(true, result)
                Mockito.verify(bookingService).deleteBooking(eqArg(1), eqArg(1))
            }
        }
}
