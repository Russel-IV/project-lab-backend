package com.team1.project_lab_backend.inventory.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Only ever used for its existence check (StayService.buildStay — docs/adr/0002,
 * docs/adr/0011): a 404 becomes "hostId not found", success is otherwise ignored, so
 * the response only needs to deserialize far enough not to blow up — the full Host
 * shape (ratings, languages) lives in identity-service and isn't needed here.
 */
@FeignClient(name = "identity-service", contextId = "inventoryHostFeignClient")
interface HostFeignClient {
    @GetMapping("/internal/hosts/{id}")
    fun get(
        @PathVariable id: Int,
    ): HostRef
}

data class HostRef(val id: Int)
