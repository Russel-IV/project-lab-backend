package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.ViewRequest
import com.team1.project_lab_backend.dto.ViewResponse
import com.team1.project_lab_backend.services.ViewService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/views")
class ViewController(
    private val viewService: ViewService
) {
    @GetMapping
    fun getAllViews(): ResponseEntity<List<ViewResponse>> =
        ResponseEntity.ok(viewService.getAllViews())

    @GetMapping("/{id}")
    fun getViewById(@PathVariable id: Int): ResponseEntity<ViewResponse> =
        ResponseEntity.ok(viewService.getViewById(id))

    @PostMapping
    fun createView(@RequestBody view: ViewRequest): ResponseEntity<ViewResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(viewService.createView(view))

    @PutMapping("/{id}")
    fun updateView(@PathVariable id: Int, @RequestBody view: ViewRequest): ResponseEntity<ViewResponse> =
        ResponseEntity.ok(viewService.updateView(id, view))

    @DeleteMapping("/{id}")
    fun deleteView(@PathVariable id: Int): ResponseEntity<Unit> =
        viewService.deleteView(id).let { ResponseEntity.noContent().build() }
}
