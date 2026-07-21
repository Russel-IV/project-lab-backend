package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.dto.CreateBookingRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.models.PaymentIntent
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import com.team1.project_lab_backend.booking.repositories.PaymentIntentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

class BookingServiceTest {
    private val bookingRepository = Mockito.mock(BookingRepository::class.java)
    private val roomFeignClient = Mockito.mock(RoomFeignClient::class.java)
    private val paymentIntentRepository = Mockito.mock(PaymentIntentRepository::class.java)

    private val bookingService = BookingService(bookingRepository, roomFeignClient, paymentIntentRepository)

    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)
    private val dayAfterTomorrow: LocalDate = LocalDate.now().plusDays(2)

    private fun room(
        id: Int,
        stayId: Int = 1,
        sleeps: Int = 2,
    ): RoomRef =
        RoomRef(
            id = id,
            stayId = stayId,
            price = BigDecimal("100.00"),
            sleeps = sleeps,
        )

    private fun baseRequest(
        userId: Int = 1,
        roomIds: Set<Int> = setOf(10),
        checkIn: LocalDate = tomorrow,
        checkOut: LocalDate = dayAfterTomorrow,
        guests: Int = 1,
        paymentIntentId: String = "pi_mock_1",
    ) = CreateBookingRequest(
        userId = userId,
        checkInDate = checkIn,
        checkOutDate = checkOut,
        guestsCount = guests,
        roomIds = roomIds,
        paymentIntentId = paymentIntentId,
    )

    // Stubs a payment intent whose stored fields mirror the given request exactly,
    // so createBooking's payment-intent-match checks pass and validation continues
    // past them into the behavior each test actually targets.
    private fun stubPaymentIntent(
        request: CreateBookingRequest,
        amount: Int,
        bookingId: Int? = null,
    ) {
        Mockito.`when`(paymentIntentRepository.findByPaymentIntentId(request.paymentIntentId)).thenReturn(
            Optional.of(
                PaymentIntent(
                    id = 1,
                    paymentIntentId = request.paymentIntentId,
                    idempotencyKey = "key-1",
                    userId = request.userId,
                    checkInDate = request.checkInDate,
                    checkOutDate = request.checkOutDate,
                    guestsCount = request.guestsCount,
                    amount = amount,
                    clientSecret = "secret",
                    bookingId = bookingId,
                    roomIds = request.roomIds.toMutableSet(),
                ),
            ),
        )
    }

    private fun stubHappyPath(
        roomIds: Set<Int> = setOf(10),
        sleeps: Int = 2,
    ): Booking {
        val rooms = roomIds.map { room(it, sleeps = sleeps) }
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(rooms)
        Mockito.`when`(
            bookingRepository.findConflictingRoomIds(anyArg(), anyArg(), anyArg(), anyArg()),
        ).thenReturn(emptySet())
        val saved =
            Booking(
                id = 99,
                userId = 1,
                checkInDate = tomorrow,
                checkOutDate = dayAfterTomorrow,
                status = BookingStatus.PENDING,
                guestsCount = 1,
                totalPrice = BigDecimal("100.00") * roomIds.size.toBigDecimal(),
                roomIds = roomIds.toMutableSet(),
            )
        Mockito.`when`(bookingRepository.save(Mockito.any(Booking::class.java))).thenReturn(saved)
        return saved
    }

    // ---- createBooking ----

    @Test
    fun createBookingReturnsPersistedBooking() {
        stubHappyPath()
        val request = baseRequest()
        stubPaymentIntent(request, amount = 10000)

        val result = bookingService.createBooking(request)

        assertEquals(99, result.id)
        assertEquals(BookingStatus.PENDING, result.status)
        assertEquals(tomorrow, result.checkInDate)
    }

    @Test
    fun createBookingCapturesTotalPrice() {
        stubHappyPath() // 1 room at $100, tomorrow → dayAfterTomorrow = 1 night
        val request = baseRequest()
        stubPaymentIntent(request, amount = 10000)
        val captor = ArgumentCaptor.forClass(Booking::class.java)

        bookingService.createBooking(request)

        Mockito.verify(bookingRepository).save(captor.capture())
        assertEquals(BigDecimal("100.00"), captor.value.totalPrice)
    }

    @Test
    fun createBookingReplaysExistingBookingWhenPaymentIntentAlreadyConsumed() {
        val request = baseRequest()
        stubPaymentIntent(request, amount = 10000, bookingId = 42)
        val existing =
            Booking(
                id = 42,
                userId = 1,
                checkInDate = tomorrow,
                checkOutDate = dayAfterTomorrow,
                status = BookingStatus.PENDING,
                guestsCount = 1,
                totalPrice = BigDecimal("100.00"),
                roomIds = mutableSetOf(10),
            )
        Mockito.`when`(bookingRepository.findById(42)).thenReturn(Optional.of(existing))

        val result = bookingService.createBooking(request)

        assertEquals(42, result.id)
        Mockito.verify(bookingRepository, Mockito.never()).save(anyArg())
    }

    @Test
    fun createBookingRejectsUnknownPaymentIntent() {
        val request = baseRequest()
        Mockito.`when`(paymentIntentRepository.findByPaymentIntentId(request.paymentIntentId)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsPaymentIntentBelongingToAnotherUser() {
        val request = baseRequest(userId = 1)
        stubPaymentIntent(request.copy(userId = 2), amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun createBookingRejectsRoomIdsNotMatchingPaymentIntent() {
        val request = baseRequest(roomIds = setOf(10))
        stubPaymentIntent(request.copy(roomIds = setOf(11)), amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsAmountNoLongerMatchingPaymentIntent() {
        stubHappyPath() // rooms priced at $100/night in RoomFeignClient
        val request = baseRequest()
        stubPaymentIntent(request, amount = 999999) // stale amount from before a price change

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsCheckInInPast() {
        val request = baseRequest(checkIn = LocalDate.now().minusDays(1))
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsCheckInMoreThanSixMonthsAway() {
        val request = baseRequest(checkIn = LocalDate.now().plusMonths(7))
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsCheckOutNotAfterCheckIn() {
        val request = baseRequest(checkIn = tomorrow, checkOut = tomorrow)
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsEmptyRoomIds() {
        val request = baseRequest(roomIds = emptySet())
        stubPaymentIntent(request, amount = 0)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsZeroGuestsCount() {
        val request = baseRequest(guests = 0)
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsUnknownRoomIds() {
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(emptyList())
        val request = baseRequest()
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsRoomsFromDifferentStays() {
        val mixedStayRooms = listOf(room(10, stayId = 1), room(11, stayId = 2))
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(mixedStayRooms)
        val request = baseRequest(roomIds = setOf(10, 11))
        stubPaymentIntent(request, amount = 20000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsConflictingRooms() {
        val rooms = listOf(room(10))
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(rooms)
        Mockito.`when`(
            bookingRepository.findConflictingRoomIds(anyArg(), anyArg(), anyArg(), anyArg()),
        ).thenReturn(setOf(10))
        val request = baseRequest()
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createBookingRejectsGuestsExceedingCapacity() {
        // Room sleeps 1, requesting 5 guests
        stubHappyPath(sleeps = 1)
        val request = baseRequest(guests = 5)
        stubPaymentIntent(request, amount = 10000)

        val ex = assertThrows(ResponseStatusException::class.java) { bookingService.createBooking(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- getBookingsByUser ----

    @Test
    fun getBookingsByUserDelegatesToRepositoryWithPaging() {
        val bookings =
            listOf(
                Booking(
                    id = 1,
                    userId = 1,
                    checkInDate = tomorrow,
                    checkOutDate = dayAfterTomorrow,
                    status = BookingStatus.CONFIRMED,
                    guestsCount = 1,
                    totalPrice = BigDecimal("100.00"),
                    roomIds = mutableSetOf(),
                ),
            )
        Mockito.`when`(bookingRepository.findByUserId(Mockito.eq(1), anyArg())).thenReturn(bookings)

        val result = bookingService.getBookingsByUser(1, page = 0, size = 5)

        assertEquals(listOf(1), result.map { it.id })
    }

    // ---- hasCompletedBookingForStay ----

    @Test
    fun hasCompletedBookingForStayReturnsTrueWhenCompletedBookingExists() {
        Mockito.`when`(bookingRepository.findRoomIdsForUserWithStatus(1, BookingStatus.COMPLETED)).thenReturn(setOf(10))
        Mockito.`when`(
            roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()),
        ).thenReturn(listOf(room(10, stayId = 2)))

        val result = bookingService.hasCompletedBookingForStay(1, 2)

        assertEquals(true, result)
    }

    @Test
    fun hasCompletedBookingForStayReturnsFalseWhenNoneExists() {
        Mockito.`when`(bookingRepository.findRoomIdsForUserWithStatus(1, BookingStatus.COMPLETED)).thenReturn(emptySet())

        val result = bookingService.hasCompletedBookingForStay(1, 2)

        assertEquals(false, result)
    }

    @Test
    fun hasCompletedBookingForStayReturnsFalseWhenRoomsBelongToOtherStays() {
        Mockito.`when`(bookingRepository.findRoomIdsForUserWithStatus(1, BookingStatus.COMPLETED)).thenReturn(setOf(10))
        Mockito.`when`(
            roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()),
        ).thenReturn(listOf(room(10, stayId = 3)))

        val result = bookingService.hasCompletedBookingForStay(1, 2)

        assertEquals(false, result)
    }

    // ---- updateBookingStatus ----

    @Test
    fun updateBookingStatusReturnsUpdatedBooking() {
        val existing =
            Booking(
                id = 5, userId = 1, checkInDate = tomorrow, checkOutDate = dayAfterTomorrow,
                status = BookingStatus.PENDING, guestsCount = 1,
                createdAt = LocalDateTime.now(), totalPrice = BigDecimal("200.00"), roomIds = mutableSetOf(),
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

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                bookingService.updateBookingStatus(99, BookingStatusRequest(BookingStatus.CANCELLED))
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteBooking ----

    @Test
    fun deleteBookingReturnsNotFoundWhenMissing() {
        Mockito.`when`(bookingRepository.findById(99)).thenReturn(Optional.empty())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                bookingService.deleteBooking(99, 1)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteBookingInvokesRepository() {
        val booking =
            Booking(
                id = 5,
                userId = 1,
                checkInDate = tomorrow,
                checkOutDate = dayAfterTomorrow,
                status = BookingStatus.PENDING,
                guestsCount = 1,
                totalPrice = BigDecimal("100.00"),
                roomIds = mutableSetOf(),
            )
        Mockito.`when`(bookingRepository.findById(5)).thenReturn(Optional.of(booking))

        bookingService.deleteBooking(5, 1)

        Mockito.verify(bookingRepository).deleteById(5)
    }
}

// Booking is not a data class here (open class, per JPA/Hibernate needs) — provide copy manually
private fun Booking.copy(status: BookingStatus) =
    Booking(
        id = id, userId = userId, checkInDate = checkInDate, checkOutDate = checkOutDate,
        status = status, guestsCount = guestsCount, createdAt = createdAt, totalPrice = totalPrice, roomIds = roomIds,
    )
