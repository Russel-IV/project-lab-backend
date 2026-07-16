package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "accessibilityFeignClient")
interface AccessibilityFeignClient {
    @GetMapping("/internal/accessibilities")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<Accessibility>

    @GetMapping("/internal/accessibilities/{id}")
    fun get(
        @PathVariable id: Int,
    ): Accessibility

    @PostMapping("/internal/accessibilities")
    fun create(
        @RequestBody request: AccessibilityRequest,
    ): Accessibility

    @PatchMapping("/internal/accessibilities/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: AccessibilityRequest,
    ): Accessibility

    @DeleteMapping("/internal/accessibilities/{id}")
    fun delete(
        @PathVariable id: Int,
    )
}
