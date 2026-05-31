package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.dto.StayResponse
import com.team1.project_lab_backend.services.StayService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stays")
class StayController(
    private val stayService: StayService
) {
    @GetMapping
    fun getAllStays(): ResponseEntity<List<StayResponse>> =
        ResponseEntity.ok(stayService.getAllStays())

    @GetMapping("/{id}")
    fun getStayById(@PathVariable id: Int): ResponseEntity<StayResponse> =
        ResponseEntity.ok(stayService.getStayById(id))

    @PostMapping
    fun createStay(@RequestBody stay: StayRequest): ResponseEntity<StayResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(stayService.createStay(stay))

    @PutMapping("/{id}")
    fun updateStay(@PathVariable id: Int, @RequestBody stay: StayRequest): ResponseEntity<StayResponse> =
        ResponseEntity.ok(stayService.updateStay(id, stay))

    @DeleteMapping("/{id}")
    fun deleteStay(@PathVariable id: Int): ResponseEntity<Unit> =
        stayService.deleteStay(id).let { ResponseEntity.noContent().build() }
}
