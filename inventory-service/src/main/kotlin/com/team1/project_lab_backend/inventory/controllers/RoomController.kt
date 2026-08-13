package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.dto.RoomResponse
import com.team1.project_lab_backend.inventory.services.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/internal/rooms")
class RoomController(private val roomService: RoomService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(required = false) stayId: Int?,
        @RequestParam(required = false) stayIds: List<Int>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<RoomResponse> =
        when {
            ids != null -> roomService.getRoomsByIds(ids)
            stayId != null -> roomService.getRoomsForStay(stayId, page, size)
            stayIds != null -> roomService.getRoomsByStayIds(stayIds)
            else -> emptyList()
        }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): RoomResponse = roomService.getRoomById(id)

    @GetMapping("/available")
    fun available(
        @RequestParam stayId: Int,
        @RequestParam checkIn: LocalDate,
        @RequestParam checkOut: LocalDate,
        @RequestParam(required = false) guests: Int?,
    ): List<RoomResponse> = roomService.getAvailableRooms(stayId, checkIn, checkOut, guests)

    @PostMapping
    fun create(
        @RequestParam stayId: Int,
        @RequestBody request: RoomRequest,
        @RequestParam requestingUserId: Int,
    ): RoomResponse = roomService.createRoom(stayId, request, requestingUserId)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: RoomRequest,
        @RequestParam requestingUserId: Int,
    ): RoomResponse = roomService.updateRoom(id, request, requestingUserId)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
    ): ResponseEntity<Void> {
        roomService.deleteRoom(id, requestingUserId)
        return ResponseEntity.noContent().build()
    }
}
