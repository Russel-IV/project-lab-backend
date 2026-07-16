package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import feign.FeignException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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

class BookingServiceTest {
    private val bookingFeignClient = Mockito.mock(BookingFeignClient::class.java)
    private val bookingService = BookingService(bookingFeignClient)

    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)
    private val dayAfterTomorrow: LocalDate = LocalDate.now().plusDays(2)

    private fun sampleBooking(
        id: Int = 1,
        userId: Int = 1,
    ) = Booking(
        id = id,
        userId = userId,
        checkInDate = tomorrow,
        checkOutDate = dayAfterTomorrow,
        status = BookingStatus.PENDING,
        guestsCount = 2,
        createdAt = LocalDateTime.now(),
        totalPrice = BigDecimal("200.00"),
        roomIds = setOf(5),
    )

    @Test
    fun getAllBookingsDelegatesToFeignClient() {
        val page = listOf(sampleBooking(1), sampleBooking(2))
        Mockito.`when`(bookingFeignClient.list(null, null, 0, 2)).thenReturn(page)

        val result = bookingService.getAllBookings(0, 2)

        assertEquals(listOf(1, 2), result.map { it.id })
    }

    @Test
    fun getBookingByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(bookingFeignClient.get(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.getBookingById(99) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getBookingsByUserDelegatesToFeignClient() {
        val page = listOf(sampleBooking(1))
        Mockito.`when`(bookingFeignClient.list(null, 1, 0, 5)).thenReturn(page)

        val result = bookingService.getBookingsByUser(1, page = 0, size = 5)

        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun hasCompletedBookingForStayDelegatesToFeignClient() {
        Mockito.`when`(bookingFeignClient.hasCompletedBookingForStay(1, 2)).thenReturn(true)

        val result = bookingService.hasCompletedBookingForStay(1, 2)

        assertEquals(true, result)
    }

    @Test
    fun createBookingDelegatesToFeignClient() {
        val saved = sampleBooking(99)
        Mockito.`when`(bookingFeignClient.create(anyArg())).thenReturn(saved)

        val result =
            bookingService.createBooking(
                BookingRequest(
                    userId = 1,
                    checkInDate = tomorrow,
                    checkOutDate = dayAfterTomorrow,
                    guestsCount = 2,
                    roomIds = setOf(5),
                ),
            )

        assertEquals(99, result.id)
    }

    @Test
    fun createBookingTranslatesBadRequestFromBookingService() {
        Mockito.`when`(bookingFeignClient.create(anyArg())).thenThrow(FeignException.BadRequest::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                bookingService.createBooking(
                    BookingRequest(
                        userId = 1,
                        checkInDate = tomorrow,
                        checkOutDate = dayAfterTomorrow,
                        guestsCount = 2,
                        roomIds = setOf(5),
                    ),
                )
            }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateBookingStatusReturnsNotFoundWhenMissing() {
        Mockito.`when`(bookingFeignClient.updateStatus(Mockito.eq(99), anyArg())).thenThrow(FeignException.NotFound::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                bookingService.updateBookingStatus(99, BookingStatusRequest(BookingStatus.CANCELLED))
            }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteBookingReturnsNotFoundWhenMissing() {
        Mockito.doThrow(FeignException.NotFound::class.java).`when`(bookingFeignClient).delete(99, 1)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.deleteBooking(99, 1) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteBookingReturnsForbiddenWhenNotOwner() {
        Mockito.doThrow(FeignException.Forbidden::class.java).`when`(bookingFeignClient).delete(5, 2)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.deleteBooking(5, 2) }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun deleteBookingInvokesFeignClient() {
        bookingService.deleteBooking(5, 1)

        Mockito.verify(bookingFeignClient).delete(5, 1)
    }
}
