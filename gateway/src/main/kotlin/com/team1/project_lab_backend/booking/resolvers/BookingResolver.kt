package com.team1.project_lab_backend.booking.resolvers

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
import com.team1.project_lab_backend.booking.dto.PaymentIntentRequest
import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.services.BookingService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Controller
class BookingResolver(private val bookingService: BookingService) {
    @SchemaMapping(typeName = "Booking", field = "createdAt")
    fun createdAt(booking: Booking): OffsetDateTime = booking.createdAt.atOffset(ZoneOffset.UTC)

    @QueryMapping
    suspend fun bookings(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Booking> = bookingService.getAllBookings(page ?: 0, size ?: 20)

    @QueryMapping
    suspend fun booking(
        @Argument id: Int,
    ): Booking = bookingService.getBookingById(id)

    @QueryMapping
    suspend fun myBookings(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Booking> {
        val currentUser = requireAuthenticated()
        return bookingService.getBookingsByUser(currentUser.id, page ?: 0, size ?: 20)
    }

    @QueryMapping
    suspend fun myBookingStatusForStay(
        @Argument stayId: Int,
    ): BookingStatusForStay {
        val currentUser = requireAuthenticated()
        return BookingStatusForStay(bookingService.hasCompletedBookingForStay(currentUser.id, stayId))
    }

    @MutationMapping
    suspend fun createPaymentIntent(
        @Argument input: CreatePaymentIntentInput,
    ): PaymentIntentPayload {
        val currentUser = requireAuthenticated()
        val result =
            bookingService.createPaymentIntent(
                PaymentIntentRequest(
                    userId = currentUser.id,
                    roomIds = input.roomIds,
                    checkInDate = input.checkInDate,
                    checkOutDate = input.checkOutDate,
                    guestsCount = input.guestsCount,
                    idempotencyKey = input.idempotencyKey,
                ),
            )
        return PaymentIntentPayload(
            clientSecret = result.clientSecret,
            paymentIntentId = result.paymentIntentId,
            amount = result.amount,
            currency = result.currency,
        )
    }

    @MutationMapping
    suspend fun createBooking(
        @Argument input: CreateBookingInput,
    ): Booking {
        val currentUser = requireAuthenticated()
        return bookingService.createBooking(
            BookingRequest(
                userId = currentUser.id,
                checkInDate = input.checkInDate,
                checkOutDate = input.checkOutDate,
                guestsCount = input.guestsCount,
                roomIds = input.roomIds,
                paymentIntentId = input.paymentIntentId,
            ),
        )
    }

    @MutationMapping
    suspend fun updateBookingStatus(
        @Argument id: Int,
        @Argument status: BookingStatus,
    ): Booking {
        requireAuthenticated()
        return bookingService.updateBookingStatus(id, BookingStatusRequest(status = status))
    }

    @MutationMapping
    suspend fun deleteBooking(
        @Argument id: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        bookingService.deleteBooking(id, currentUser.id)
        return true
    }
}

data class CreateBookingInput(
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>,
    val paymentIntentId: String,
)

data class CreatePaymentIntentInput(
    val roomIds: Set<Int>,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val idempotencyKey: String,
)

data class PaymentIntentPayload(
    val clientSecret: String,
    val paymentIntentId: String,
    val amount: Int,
    val currency: String,
)

data class BookingStatusForStay(val hasCompletedBooking: Boolean)
