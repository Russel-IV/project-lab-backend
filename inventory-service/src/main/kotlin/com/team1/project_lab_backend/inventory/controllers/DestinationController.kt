package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.models.Destination
import com.team1.project_lab_backend.inventory.services.DestinationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/destinations")
class DestinationController(private val destinationService: DestinationService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "20") limit: Int,
    ): List<Destination> = destinationService.searchDestinations(search, limit)

    @GetMapping("/popular")
    fun popular(
        @RequestParam(defaultValue = "8") limit: Int,
    ): List<Destination> = destinationService.popularDestinations(limit)
}
