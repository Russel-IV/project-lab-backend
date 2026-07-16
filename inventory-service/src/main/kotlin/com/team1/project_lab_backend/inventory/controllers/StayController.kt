package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.dto.StayResponse
import com.team1.project_lab_backend.inventory.services.StayService
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

/**
 * Internal-only API (docs/adr/0005) — the Gateway's StayFeignClient is the only
 * caller. Search takes a POST body (StayFilter has too many optional fields for a
 * clean query-string contract), matching CreateReviewRequest-style plain bodies used
 * elsewhere in this internal API.
 */
@RestController
@RequestMapping("/internal/stays")
class StayController(private val stayService: StayService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<StayResponse> = if (ids != null) stayService.getStaysByIds(ids) else emptyList()

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): StayResponse = stayService.getStayById(id)

    @PostMapping("/search")
    fun search(
        @RequestBody filter: StayFilter,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<StayResponse> = stayService.searchStays(filter, page, size)

    @PostMapping
    fun create(
        @RequestBody request: StayRequest,
        @RequestParam requestingUserId: Int,
    ): StayResponse = stayService.createStay(request, requestingUserId)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: StayRequest,
        @RequestParam requestingUserId: Int,
    ): StayResponse = stayService.updateStay(id, request, requestingUserId)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
    ): ResponseEntity<Void> {
        stayService.deleteStay(id, requestingUserId)
        return ResponseEntity.noContent().build()
    }
}
