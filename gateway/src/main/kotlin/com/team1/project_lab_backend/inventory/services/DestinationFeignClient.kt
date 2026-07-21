package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(name = "inventory-service", contextId = "destinationFeignClient")
interface DestinationFeignClient {
    @GetMapping("/internal/destinations")
    fun list(): List<Destination>
}
