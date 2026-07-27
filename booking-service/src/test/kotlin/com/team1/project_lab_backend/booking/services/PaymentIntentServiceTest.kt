package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.CreatePaymentIntentRequest
import com.team1.project_lab_backend.booking.models.PaymentIntent
import com.team1.project_lab_backend.booking.repositories.PaymentIntentRepository
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
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

class PaymentIntentServiceTest {
    private val paymentIntentRepository = Mockito.mock(PaymentIntentRepository::class.java)
    private val roomFeignClient = Mockito.mock(RoomFeignClient::class.java)
    private val stripeClient = Mockito.mock(StripeClient::class.java)

    private val paymentIntentService = PaymentIntentService(paymentIntentRepository, roomFeignClient, stripeClient)

    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)
    private val dayAfterTomorrow: LocalDate = LocalDate.now().plusDays(2)

    private fun room(
        id: Int,
        stayId: Int = 1,
        sleeps: Int = 2,
    ): RoomRef = RoomRef(id = id, stayId = stayId, price = BigDecimal("100.00"), sleeps = sleeps)

    private fun baseRequest(
        userId: Int = 1,
        roomIds: Set<Int> = setOf(10),
        checkIn: LocalDate = tomorrow,
        checkOut: LocalDate = dayAfterTomorrow,
        guests: Int = 1,
        idempotencyKey: String = "key-1",
    ) = CreatePaymentIntentRequest(
        userId = userId,
        roomIds = roomIds,
        checkInDate = checkIn,
        checkOutDate = checkOut,
        guestsCount = guests,
        idempotencyKey = idempotencyKey,
    )

    private fun stubNoExistingIntent(request: CreatePaymentIntentRequest) {
        Mockito.`when`(paymentIntentRepository.findByUserIdAndIdempotencyKey(request.userId, request.idempotencyKey))
            .thenReturn(Optional.empty())
    }

    @Test
    fun createPaymentIntentComputesAmountFromCurrentRoomPrices() {
        val request = baseRequest()
        stubNoExistingIntent(request)
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(listOf(room(10)))
        Mockito.`when`(stripeClient.createPaymentIntent(Mockito.anyInt(), Mockito.anyString(), anyArg(), anyArg()))
            .thenReturn(StripePaymentIntentResult(id = "pi_test_123", clientSecret = "pi_test_123_secret_abc"))
        Mockito.`when`(paymentIntentRepository.save(Mockito.any(PaymentIntent::class.java)))
            .thenAnswer { it.arguments[0] }

        val result = paymentIntentService.createPaymentIntent(request)

        assertEquals(10000, result.amount)
        assertEquals("usd", result.currency)
        assertEquals("pi_test_123", result.paymentIntentId)
        assertEquals("pi_test_123_secret_abc", result.clientSecret)
    }

    @Test
    fun createPaymentIntentReplaysExistingIntentForSameIdempotencyKey() {
        val request = baseRequest()
        val existing =
            PaymentIntent(
                id = 1,
                paymentIntentId = "pi_mock_existing",
                idempotencyKey = request.idempotencyKey,
                userId = request.userId,
                checkInDate = request.checkInDate,
                checkOutDate = request.checkOutDate,
                guestsCount = request.guestsCount,
                amount = 10000,
                clientSecret = "secret",
                roomIds = request.roomIds.toMutableSet(),
            )
        Mockito.`when`(paymentIntentRepository.findByUserIdAndIdempotencyKey(request.userId, request.idempotencyKey))
            .thenReturn(Optional.of(existing))

        val result = paymentIntentService.createPaymentIntent(request)

        assertEquals("pi_mock_existing", result.paymentIntentId)
        Mockito.verify(paymentIntentRepository, Mockito.never()).save(anyArg())
    }

    @Test
    fun createPaymentIntentRejectsEmptyRoomIds() {
        val request = baseRequest(roomIds = emptySet())
        stubNoExistingIntent(request)

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsCheckInInPast() {
        // Beyond the 1-day cross-timezone grace window (see PaymentIntentService.createPaymentIntent).
        val request = baseRequest(checkIn = LocalDate.now().minusDays(3))
        stubNoExistingIntent(request)

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsCheckOutNotAfterCheckIn() {
        val request = baseRequest(checkIn = tomorrow, checkOut = tomorrow)
        stubNoExistingIntent(request)

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsUnknownRoomIds() {
        val request = baseRequest()
        stubNoExistingIntent(request)
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(emptyList())

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsRoomsFromDifferentStays() {
        val request = baseRequest(roomIds = setOf(10, 11))
        stubNoExistingIntent(request)
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(listOf(room(10, stayId = 1), room(11, stayId = 2)))

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsGuestsExceedingCapacity() {
        val request = baseRequest(guests = 5)
        stubNoExistingIntent(request)
        Mockito.`when`(roomFeignClient.list(anyArg(), anyArg(), anyArg(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(listOf(room(10, sleeps = 1)))

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createPaymentIntentRejectsZeroGuestsCount() {
        val request = baseRequest(guests = 0)
        stubNoExistingIntent(request)

        val ex = assertThrows(ResponseStatusException::class.java) { paymentIntentService.createPaymentIntent(request) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }
}
