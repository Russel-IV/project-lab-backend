package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@FeignClient(name = "inventory-service", contextId = "roomFeignClient")
interface RoomFeignClient {

    @GetMapping("/internal/rooms")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(required = false) stayId: Int?,
        @RequestParam(required = false) stayIds: List<Int>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<Room>

    @GetMapping("/internal/rooms/{id}")
    fun get(@PathVariable id: Int): Room

    @GetMapping("/internal/rooms/available")
    fun available(
        @RequestParam stayId: Int,
        @RequestParam checkIn: LocalDate,
        @RequestParam checkOut: LocalDate,
        @RequestParam(required = false) guests: Int?,
    ): List<Room>

    @PostMapping("/internal/rooms")
    fun create(
        @RequestParam stayId: Int,
        @RequestBody request: RoomRequest,
        @RequestParam requestingUserId: Int,
    ): Room

    @PatchMapping("/internal/rooms/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: RoomRequest, @RequestParam requestingUserId: Int): Room

    @DeleteMapping("/internal/rooms/{id}")
    fun delete(@PathVariable id: Int, @RequestParam requestingUserId: Int)
}
