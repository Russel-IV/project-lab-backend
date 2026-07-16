package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.View
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "viewFeignClient")
interface ViewFeignClient {
    @GetMapping("/internal/views")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<View>

    @GetMapping("/internal/views/{id}")
    fun get(
        @PathVariable id: Int,
    ): View

    @PostMapping("/internal/views")
    fun create(
        @RequestBody request: ViewRequest,
    ): View

    @PatchMapping("/internal/views/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: ViewRequest,
    ): View

    @DeleteMapping("/internal/views/{id}")
    fun delete(
        @PathVariable id: Int,
    )
}
