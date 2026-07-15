package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.repositories.RoomRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository,
    private val bookingAvailabilityClient: BookingAvailabilityClient,
) {
    @Transactional(readOnly = true)
    fun getRoomsForStay(
        stayId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Room> {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return roomRepository.findByStayId(stayId, PageRequest.of(page, size))
    }

    @Transactional(readOnly = true)
    fun getRoomById(id: Int): Room {
        id.requirePositive()
        return roomRepository.findById(id).orNotFound("room not found")
    }

    @Transactional(readOnly = true)
    fun getRoomsByIds(ids: List<Int>): List<Room> = roomRepository.findAllById(ids)

    // Backs the Gateway's StayBatchResolver.rooms()/startingFromPrice() — bulk rooms
    // across many stays at once (as opposed to getRoomsForStay's single, paginated
    // stay), same shape as StayRepository's other findByIdInWithX bulk queries.
    @Transactional(readOnly = true)
    fun getRoomsByStayIds(stayIds: List<Int>): List<Room> = roomRepository.findByStayIdIn(stayIds)

    @Transactional
    fun createRoom(
        stayId: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room {
        stayId.requirePositive("stayId")
        val stay = stayRepository.findById(stayId).orNotFound("stay not found")
        if (stay.hostId != requestingUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
        validateRoomRequest(request)
        return roomRepository.save(
            Room(
                id = 0,
                stayId = stayId,
                name = request.name,
                price = request.price,
                sleeps = request.sleeps,
                bedroomAmount = request.bedroomAmount,
                bathrooms = request.bathrooms,
                size = request.size,
            ),
        )
    }

    @Transactional
    fun updateRoom(
        id: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room {
        id.requirePositive()
        val existing = roomRepository.findById(id).orNotFound("room not found")
        val stay = stayRepository.findById(existing.stayId).orNotFound("stay not found")
        if (stay.hostId != requestingUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
        validateRoomRequest(request)
        return roomRepository.save(
            Room(
                id = id,
                stayId = existing.stayId,
                name = request.name,
                price = request.price,
                sleeps = request.sleeps,
                bedroomAmount = request.bedroomAmount,
                bathrooms = request.bathrooms,
                size = request.size,
            ),
        )
    }

    @Transactional
    fun deleteRoom(
        id: Int,
        requestingUserId: Int,
    ) {
        id.requirePositive()
        val room = roomRepository.findById(id).orNotFound("room not found")
        val stay = stayRepository.findById(room.stayId).orNotFound("stay not found")
        if (stay.hostId != requestingUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
        roomRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAvailableRooms(
        stayId: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int? = null,
    ): List<Room> {
        stayId.requirePositive("stayId")
        if (!checkOut.isAfter(checkIn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn")
        }
        guests?.let {
            if (it < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "guests must be at least 1")
        }
        val stayRooms = roomRepository.findByStayId(stayId)
        if (stayRooms.isEmpty()) return emptyList()
        // Room<->Booking availability moved behind a Resilience4j-wrapped Feign call
        // (docs/adr/0010) — see BookingAvailabilityClient/StayService.buildSpec for
        // the same pattern applied to search.
        val conflictingRoomIds = bookingAvailabilityClient.getConflictingRoomIds(stayRooms.map { it.id }, checkIn, checkOut)
        return stayRooms.filter { it.id !in conflictingRoomIds && (guests == null || it.sleeps >= guests) }
    }

    private fun validateRoomRequest(request: RoomRequest) {
        request.name.requireNotBlank("name")
        request.price.requireNonNegative("price")
        request.sleeps.requirePositive("sleeps")
        request.bedroomAmount.requireNonNegative("bedroomAmount")
        request.bathrooms.requireNonNegative("bathrooms")
        request.size?.requireNonNegative("size")
    }
}
