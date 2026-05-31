package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.HostRequest
import com.team1.project_lab_backend.dto.HostResponse
import com.team1.project_lab_backend.services.HostService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/hosts")
class HostController(
    private val hostService: HostService
) {
    @GetMapping
    fun getAllHosts(): ResponseEntity<List<HostResponse>> =
        ResponseEntity.ok(hostService.getAllHosts())

    @GetMapping("/{id}")
    fun getHostById(@PathVariable id: Int): ResponseEntity<HostResponse> =
        ResponseEntity.ok(hostService.getHostById(id))

    @PostMapping
    fun createHost(@RequestBody host: HostRequest): ResponseEntity<HostResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(hostService.createHost(host))

    @PutMapping("/{id}")
    fun updateHost(@PathVariable id: Int, @RequestBody host: HostRequest): ResponseEntity<HostResponse> =
        ResponseEntity.ok(hostService.updateHost(id, host))

    @DeleteMapping("/{id}")
    fun deleteHost(@PathVariable id: Int): ResponseEntity<Unit> =
        hostService.deleteHost(id).let { ResponseEntity.noContent().build() }
}
