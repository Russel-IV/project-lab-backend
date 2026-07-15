package com.team1.project_lab_backend.booking.resolvers

import com.team1.project_lab_backend.booking.dto.BookingRequest
import com.team1.project_lab_backend.booking.dto.BookingStatusRequest
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

    // Booking.createdAt is stored as a timezone-less LocalDateTime, but the
    // GraphQL DateTime scalar (ExtendedScalars.DateTime) only serializes
    // OffsetDateTime — without this mapping, createBooking/booking/bookings
    // fail at the createdAt field with a scalar coercion error.
    @SchemaMapping(typeName = "Booking", field = "createdAt")
    fun createdAt(booking: Booking): OffsetDateTime =
        booking.createdAt.atOffset(ZoneOffset.UTC)

    @QueryMapping
    fun bookings(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Booking> = bookingService.getAllBookings(page ?: 0, size ?: 20)

    @QueryMapping
    fun booking(@Argument id: Int): Booking = bookingService.getBookingById(id)

    @QueryMapping
    fun myBookings(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Booking> {
        val currentUser = requireAuthenticated()
        return bookingService.getBookingsByUser(currentUser.id, page ?: 0, size ?: 20)
    }

    @QueryMapping
    fun myBookingStatusForStay(@Argument stayId: Int): BookingStatusForStay {
        val currentUser = requireAuthenticated()
        return BookingStatusForStay(bookingService.hasCompletedBookingForStay(currentUser.id, stayId))
    }

    @MutationMapping
    fun createBooking(@Argument input: CreateBookingInput): Booking {
        val currentUser = requireAuthenticated()
        return bookingService.createBooking(
            BookingRequest(
                userId = currentUser.id,
                checkInDate = input.checkInDate,
                checkOutDate = input.checkOutDate,
                guestsCount = input.guestsCount,
                roomIds = input.roomIds,
            ),
        )
    }

    @MutationMapping
    fun updateBookingStatus(@Argument id: Int, @Argument status: BookingStatus): Booking {
        requireAuthenticated()
        return bookingService.updateBookingStatus(id, BookingStatusRequest(status = status))
    }

    @MutationMapping
    fun deleteBooking(@Argument id: Int): Boolean {
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
)

data class BookingStatusForStay(val hasCompletedBooking: Boolean)
