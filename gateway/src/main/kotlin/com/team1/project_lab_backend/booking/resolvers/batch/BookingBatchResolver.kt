package com.team1.project_lab_backend.booking.resolvers.batch

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserFeignClient
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class BookingBatchResolver(
    private val bookingRepository: BookingRepository,
    private val userFeignClient: UserFeignClient,
) {
    @BatchMapping
    fun user(bookings: List<Booking>): Map<Booking, User> {
        val ids = bookings.map { it.userId }.distinct()
        val loaded = userFeignClient.list(ids).associateBy { it.id }
        return bookings.associateWith { loaded[it.userId]!! }
    }

    @BatchMapping
    fun rooms(bookings: List<Booking>): Map<Booking, List<Room>> {
        val ids = bookings.map { it.id }
        val loaded = bookingRepository.findByIdInWithRooms(ids).associateBy { it.id }
        return bookings.associateWith { loaded[it.id]?.rooms?.toList() ?: emptyList() }
    }
}
