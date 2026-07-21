package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.dto.CreatePaymentIntentRequest
import com.team1.project_lab_backend.booking.dto.PaymentIntentResponse
import com.team1.project_lab_backend.booking.models.PaymentIntent
import com.team1.project_lab_backend.booking.repositories.PaymentIntentRepository
import com.team1.project_lab_backend.util.requireAllPositive
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class PaymentIntentService(
    private val paymentIntentRepository: PaymentIntentRepository,
    private val roomFeignClient: RoomFeignClient,
    private val stripeClient: StripeClient,
) {
    @Transactional
    fun createPaymentIntent(request: CreatePaymentIntentRequest): PaymentIntentResponse {
        request.userId.requirePositive("userId")
        request.idempotencyKey.requireNotBlank("idempotencyKey")

        paymentIntentRepository.findByUserIdAndIdempotencyKey(request.userId, request.idempotencyKey)
            .orElse(null)
            ?.let { return it.toResponse() }

        if (request.roomIds.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "roomIds must not be empty")
        }
        request.roomIds.requireAllPositive("roomIds")
        request.guestsCount.requirePositive("guestsCount")

        val today = LocalDate.now()
        if (request.checkInDate.isBefore(today)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate must not be in the past")
        }
        if (!request.checkOutDate.isAfter(request.checkInDate)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOutDate must be after checkInDate")
        }

        val rooms = roomFeignClient.list(ids = request.roomIds.toList(), stayId = null, stayIds = null, page = 0, size = 0)
        if (rooms.size != request.roomIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "roomIds contains unknown ids")
        }
        if (rooms.map { it.stayId }.toSet().size > 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "all rooms must belong to the same stay")
        }
        if (request.guestsCount > rooms.sumOf { it.sleeps }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "guestsCount exceeds total capacity of requested rooms")
        }

        val nights = (request.checkOutDate.toEpochDay() - request.checkInDate.toEpochDay()).toBigDecimal()
        val totalPrice = rooms.fold(BigDecimal.ZERO) { acc, room -> acc + room.price } * nights
        val amount = totalPrice.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toInt()

        val stripeIntent =
            stripeClient.createPaymentIntent(
                amount = amount,
                currency = "usd",
                idempotencyKey = request.idempotencyKey,
                metadata =
                    mapOf(
                        "roomIds" to request.roomIds.joinToString(","),
                        "checkInDate" to request.checkInDate.toString(),
                        "checkOutDate" to request.checkOutDate.toString(),
                    ),
            )

        val saved =
            paymentIntentRepository.save(
                PaymentIntent(
                    paymentIntentId = stripeIntent.id,
                    idempotencyKey = request.idempotencyKey,
                    userId = request.userId,
                    checkInDate = request.checkInDate,
                    checkOutDate = request.checkOutDate,
                    guestsCount = request.guestsCount,
                    amount = amount,
                    currency = "usd",
                    clientSecret = stripeIntent.clientSecret,
                    roomIds = request.roomIds.toMutableSet(),
                ),
            )
        return saved.toResponse()
    }
}

private fun PaymentIntent.toResponse() =
    PaymentIntentResponse(
        paymentIntentId = paymentIntentId,
        clientSecret = clientSecret,
        amount = amount,
        currency = currency,
    )
