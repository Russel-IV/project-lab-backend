package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.services.RoomService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal
import java.time.LocalDate

@Controller
class RoomResolver(private val roomService: RoomService) {

    @QueryMapping
    fun rooms(
        @Argument stayId: Int,
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Room> = roomService.getRoomsForStay(stayId, page ?: 0, size ?: 20)

    @QueryMapping
    fun room(@Argument id: Int): Room? = roomService.getRoomById(id)

    @QueryMapping
    fun availableRooms(
        @Argument stayId: Int,
        @Argument checkIn: LocalDate,
        @Argument checkOut: LocalDate,
    ): List<Room> = roomService.getAvailableRooms(stayId, checkIn, checkOut)

    @MutationMapping
    fun createRoom(@Argument stayId: Int, @Argument input: CreateRoomInput): Room {
        val currentUser = requireAuthenticated()
        return roomService.createRoom(stayId, input.toRequest(), currentUser.id)
    }

    @MutationMapping
    fun updateRoom(@Argument id: Int, @Argument input: UpdateRoomInput): Room {
        val currentUser = requireAuthenticated()
        return roomService.updateRoom(id, input.toRequest(), currentUser.id)
    }

    @MutationMapping
    fun deleteRoom(@Argument id: Int): Boolean {
        val currentUser = requireAuthenticated()
        roomService.deleteRoom(id, currentUser.id)
        return true
    }
}

data class CreateRoomInput(
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal? = null,
) {
    fun toRequest() = RoomRequest(
        name = name,
        price = price,
        sleeps = sleeps,
        bedroomAmount = bedroomAmount,
        bathrooms = bathrooms,
        size = size,
    )
}

data class UpdateRoomInput(
    val name: String,
    val price: BigDecimal,
    val sleeps: Int,
    val bedroomAmount: Int,
    val bathrooms: BigDecimal,
    val size: BigDecimal? = null,
) {
    fun toRequest() = RoomRequest(
        name = name,
        price = price,
        sleeps = sleeps,
        bedroomAmount = bedroomAmount,
        bathrooms = bathrooms,
        size = size,
    )
}
