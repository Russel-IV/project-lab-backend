package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.services.HostService
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

@RestController
@RequestMapping("/internal/hosts")
class HostController(private val hostService: HostService) {

    @GetMapping
    fun list(@RequestParam(required = false) ids: List<Int>?): List<Host> =
        if (ids != null) hostService.getHostsByIds(ids) else hostService.getAllHosts()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): Host = hostService.getHostById(id)

    @PostMapping
    fun create(@RequestBody request: HostRequest): ResponseEntity<Host> =
        ResponseEntity.status(HttpStatus.CREATED).body(hostService.createHost(request))

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: HostRequest): Host = hostService.updateHost(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int) {
        hostService.deleteHost(id)
    }
}
