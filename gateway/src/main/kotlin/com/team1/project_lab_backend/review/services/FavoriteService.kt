package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.inventory.services.StayFeignClient
import com.team1.project_lab_backend.util.requirePositive
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005), same shape as ReviewService: Favorite data
 * lives in review-service, reached via favoriteFeignClient. The one cross-domain
 * check that belongs here rather than in review-service is stayId existence on
 * addFavorite (docs/adr/0011, same pattern as ReviewService.requireStayExists) —
 * removeFavorite deliberately skips it so a stale favorite for a since-deleted
 * stay can still be cleared idempotently.
 */
@Service
class FavoriteService(
    private val favoriteFeignClient: FavoriteFeignClient,
    private val stayFeignClient: StayFeignClient,
) {
    fun getMyFavoriteStayIds(userId: Int): List<Int> = favoriteFeignClient.list(userId)

    fun addFavorite(
        userId: Int,
        stayId: Int,
    ) {
        stayId.requirePositive("stayId")
        requireStayExists(stayId)
        favoriteFeignClient.add(userId, stayId)
    }

    fun removeFavorite(
        userId: Int,
        stayId: Int,
    ) {
        stayId.requirePositive("stayId")
        favoriteFeignClient.remove(userId, stayId)
    }

    private fun requireStayExists(stayId: Int) {
        try {
            stayFeignClient.get(stayId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
    }
}
