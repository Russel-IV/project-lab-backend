package com.team1.project_lab_backend.review.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Resolves via Eureka to review-service's internal REST API (docs/adr/0005,
 * docs/adr/0008). Mirrors FavoriteController one-to-one — this interface and
 * that controller are two halves of one contract that must be kept in sync by
 * hand, since there's no shared library between the two modules.
 */
@FeignClient(name = "review-service", contextId = "favoriteFeignClient")
interface FavoriteFeignClient {
    @GetMapping("/internal/favorites")
    fun list(
        @RequestParam userId: Int,
    ): List<Int>

    @PostMapping("/internal/favorites")
    fun add(
        @RequestParam userId: Int,
        @RequestParam stayId: Int,
    )

    @DeleteMapping("/internal/favorites")
    fun remove(
        @RequestParam userId: Int,
        @RequestParam stayId: Int,
    )
}
