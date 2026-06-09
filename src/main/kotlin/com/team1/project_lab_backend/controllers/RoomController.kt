package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.RoomRequest
import com.team1.project_lab_backend.dto.RoomResponse
import com.team1.project_lab_backend.services.RoomService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1")
class RoomController(
    private val roomService: RoomService
) {
    @GetMapping("/stays/{stayId}/rooms")
    fun getRoomsForStay(@PathVariable stayId: Int): ResponseEntity<List<RoomResponse>> =
        ResponseEntity.ok(roomService.getRoomsForStay(stayId))

    @PostMapping("/stays/{stayId}/rooms")
    fun createRoom(
        @PathVariable stayId: Int,
        @RequestBody request: RoomRequest
    ): ResponseEntity<RoomResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(stayId, request))

    @GetMapping("/rooms/{id}")
    fun getRoomById(@PathVariable id: Int): ResponseEntity<RoomResponse> =
        ResponseEntity.ok(roomService.getRoomById(id))

    @PutMapping("/rooms/{id}")
    fun updateRoom(
        @PathVariable id: Int,
        @RequestBody request: RoomRequest
    ): ResponseEntity<RoomResponse> =
        ResponseEntity.ok(roomService.updateRoom(id, request))

    @DeleteMapping("/rooms/{id}")
    fun deleteRoom(@PathVariable id: Int): ResponseEntity<Unit> =
        roomService.deleteRoom(id).let { ResponseEntity.noContent().build() }

    @GetMapping("/stays/{stayId}/availability")
    fun getAvailableRooms(
        @PathVariable stayId: Int,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate
    ): ResponseEntity<List<RoomResponse>> =
        ResponseEntity.ok(roomService.getAvailableRooms(stayId, checkIn, checkOut))
}
