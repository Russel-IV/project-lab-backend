package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.models.Favorite
import com.team1.project_lab_backend.review.repositories.FavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Both mutations are idempotent by design: addFavorite always ends in the
 * "favorited" state and removeFavorite always ends in the "not favorited"
 * state, regardless of the prior state — no conflict/not-found thrown for a
 * repeat call, unlike ReviewService's duplicate-review check.
 */
@Service
class FavoriteService(private val favoriteRepository: FavoriteRepository) {
    @Transactional(readOnly = true)
    fun getFavoriteStayIds(userId: Int): List<Int> = favoriteRepository.findByUserId(userId).map { it.stayId }

    @Transactional
    fun addFavorite(
        userId: Int,
        stayId: Int,
    ) {
        if (!favoriteRepository.existsByUserIdAndStayId(userId, stayId)) {
            favoriteRepository.save(Favorite(userId = userId, stayId = stayId))
        }
    }

    @Transactional
    fun removeFavorite(
        userId: Int,
        stayId: Int,
    ) {
        favoriteRepository.deleteByUserIdAndStayId(userId, stayId)
    }
}
