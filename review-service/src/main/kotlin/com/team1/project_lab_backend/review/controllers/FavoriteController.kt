package com.team1.project_lab_backend.review.controllers

import com.team1.project_lab_backend.review.services.FavoriteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Internal-only API (docs/adr/0005) — the Gateway's FavoriteFeignClient is the
 * only caller. Not part of the public contract, so the shape here is whatever's
 * convenient for that one caller, not a versioned public REST API.
 */
@RestController
@RequestMapping("/internal/favorites")
class FavoriteController(private val favoriteService: FavoriteService) {
    @GetMapping
    fun list(
        @RequestParam userId: Int,
    ): List<Int> = favoriteService.getFavoriteStayIds(userId)

    @PostMapping
    fun add(
        @RequestParam userId: Int,
        @RequestParam stayId: Int,
    ): ResponseEntity<Void> {
        favoriteService.addFavorite(userId, stayId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun remove(
        @RequestParam userId: Int,
        @RequestParam stayId: Int,
    ): ResponseEntity<Void> {
        favoriteService.removeFavorite(userId, stayId)
        return ResponseEntity.noContent().build()
    }
}
