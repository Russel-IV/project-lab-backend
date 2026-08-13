package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import com.team1.project_lab_backend.inventory.services.AccessibilityService
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

@RestController
@RequestMapping("/internal/accessibilities")
class AccessibilityController(private val accessibilityService: AccessibilityService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<Accessibility> =
        if (ids != null) accessibilityService.getAllById(ids) else accessibilityService.getAllAccessibility()

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): Accessibility = accessibilityService.getAccessibilityById(id)

    @PostMapping
    fun create(
        @RequestBody request: AccessibilityRequest,
    ): Accessibility = accessibilityService.createAccessibility(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: AccessibilityRequest,
    ): Accessibility = accessibilityService.updateAccessibility(id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
    ): ResponseEntity<Void> {
        accessibilityService.deleteAccessibility(id)
        return ResponseEntity.noContent().build()
    }
}
