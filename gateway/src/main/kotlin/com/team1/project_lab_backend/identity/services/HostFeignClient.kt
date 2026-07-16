package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.Host
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "identity-service", contextId = "hostFeignClient")
interface HostFeignClient {
    @GetMapping("/internal/hosts")
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<Host>

    @GetMapping("/internal/hosts/{id}")
    fun get(
        @PathVariable id: Int,
    ): Host

    @PostMapping("/internal/hosts")
    fun create(
        @RequestBody request: HostUpsertRequest,
    ): Host

    @PatchMapping("/internal/hosts/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: HostUpsertRequest,
    ): Host

    @DeleteMapping("/internal/hosts/{id}")
    fun delete(
        @PathVariable id: Int,
    )
}

data class HostUpsertRequest(
    val id: Int? = null,
    val communicationRating: java.math.BigDecimal? = null,
    val checkinProcessRating: java.math.BigDecimal? = null,
    val cancellationRate: java.math.BigDecimal? = null,
    val languageIds: Set<Int> = emptySet(),
)
