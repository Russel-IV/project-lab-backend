package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.BookingRequest
import com.team1.project_lab_backend.dto.BookingStatusRequest
import com.team1.project_lab_backend.models.Booking
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.services.BookingService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

@Controller
class BookingResolver(private val bookingService: BookingService) {

    @QueryMapping
    fun bookings(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Booking> = bookingService.getAllBookings(page ?: 0, size ?: 20)

    @QueryMapping
    fun booking(@Argument id: Int): Booking = bookingService.getBookingById(id)

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
