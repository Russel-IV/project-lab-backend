package com.team1.project_lab_backend.review.repositories

import com.team1.project_lab_backend.review.models.Favorite
import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteRepository : JpaRepository<Favorite, Int> {
    fun findByUserId(userId: Int): List<Favorite>

    fun existsByUserIdAndStayId(
        userId: Int,
        stayId: Int,
    ): Boolean

    fun deleteByUserIdAndStayId(
        userId: Int,
        stayId: Int,
    )
}
