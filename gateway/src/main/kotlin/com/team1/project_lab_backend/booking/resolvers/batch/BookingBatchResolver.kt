package com.team1.project_lab_backend.booking.resolvers.batch

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserFeignClient
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.services.RoomFeignClient
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class BookingBatchResolver(
    private val userFeignClient: UserFeignClient,
    private val roomFeignClient: RoomFeignClient,
) {
    @BatchMapping
    fun user(bookings: List<Booking>): Map<Booking, User> {
        val ids = bookings.map { it.userId }.distinct()
        val loaded = userFeignClient.list(ids).associateBy { it.id }
        return bookings.associateWith { loaded[it.userId]!! }
    }

    // Booking is now a plain DTO fully deserialized from booking-service's response
    // (docs/adr/0002) — its roomIds are already in hand from the parent query, no
    // extra round trip to booking-service is needed here, only to inventory-service
    // to resolve those ids into full Room details.
    @BatchMapping
    fun rooms(bookings: List<Booking>): Map<Booking, List<Room>> {
        val allRoomIds = bookings.flatMap { it.roomIds }.distinct()
        val loadedRooms =
            if (allRoomIds.isEmpty()) {
                emptyMap()
            } else {
                roomFeignClient.list(ids = allRoomIds, stayId = null, stayIds = null, page = 0, size = 0).associateBy { it.id }
            }
        return bookings.associateWith { booking -> booking.roomIds.mapNotNull { loadedRooms[it] } }
    }
}
