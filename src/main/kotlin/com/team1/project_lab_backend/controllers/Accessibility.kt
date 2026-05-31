package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.AccessibilityRequest
import com.team1.project_lab_backend.dto.AccessibilityResponse
import com.team1.project_lab_backend.services.AccessibilityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accessibility")
class AccessibilityController(
    private val accessibilityService: AccessibilityService
) {
    @GetMapping
    fun getAllAccessibility(): ResponseEntity<List<AccessibilityResponse>> =
        ResponseEntity.ok(accessibilityService.getAllAccessibility())

    @GetMapping("/{id}")
    fun getAccessibilityById(@PathVariable id: Int): ResponseEntity<AccessibilityResponse> =
        ResponseEntity.ok(accessibilityService.getAccessibilityById(id))

    @PostMapping
    fun createAccessibility(@RequestBody accessibility: AccessibilityRequest): ResponseEntity<AccessibilityResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(accessibilityService.createAccessibility(accessibility))

    @PutMapping("/{id}")
    fun updateAccessibility(
        @PathVariable id: Int,
        @RequestBody accessibility: AccessibilityRequest
    ): ResponseEntity<AccessibilityResponse> =
        ResponseEntity.ok(accessibilityService.updateAccessibility(id, accessibility))

    @DeleteMapping("/{id}")
    fun deleteAccessibility(@PathVariable id: Int): ResponseEntity<Unit> =
        accessibilityService.deleteAccessibility(id).let { ResponseEntity.noContent().build() }
}
