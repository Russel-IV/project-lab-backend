package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.View
import com.team1.project_lab_backend.inventory.services.ViewService
import org.springframework.http.HttpStatus
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
 * Internal-only API (docs/adr/0005) — the Gateway's ViewFeignClient is the
 * only caller.
 */
@RestController
@RequestMapping("/internal/views")
class ViewController(private val viewService: ViewService) {

    @GetMapping
    fun list(@RequestParam(required = false) ids: List<Int>?): List<View> =
        if (ids != null) viewService.getAllById(ids) else viewService.getAllViews()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): View = viewService.getViewById(id)

    @PostMapping
    fun create(@RequestBody request: ViewRequest): View = viewService.createView(request)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: ViewRequest): View =
        viewService.updateView(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Void> {
        viewService.deleteView(id)
        return ResponseEntity.noContent().build()
    }
}
