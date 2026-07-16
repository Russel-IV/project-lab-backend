package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Stay
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "stayFeignClient")
interface StayFeignClient {
    @GetMapping("/internal/stays")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<Stay>

    @GetMapping("/internal/stays/{id}")
    fun get(
        @PathVariable id: Int,
    ): Stay

    @PostMapping("/internal/stays/search")
    fun search(
        @RequestBody filter: StayFilter,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): List<Stay>

    @PostMapping("/internal/stays")
    fun create(
        @RequestBody request: StayRequest,
        @RequestParam requestingUserId: Int,
    ): Stay

    @PatchMapping("/internal/stays/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: StayRequest,
        @RequestParam requestingUserId: Int,
    ): Stay

    @DeleteMapping("/internal/stays/{id}")
    fun delete(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
    )
}
