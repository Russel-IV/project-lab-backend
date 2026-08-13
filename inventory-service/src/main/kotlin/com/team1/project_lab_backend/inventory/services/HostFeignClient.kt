package com.team1.project_lab_backend.inventory.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "identity-service", contextId = "inventoryHostFeignClient")
interface HostFeignClient {
    @GetMapping("/internal/hosts/{id}")
    fun get(
        @PathVariable id: Int,
    ): HostRef
}

data class HostRef(val id: Int)
