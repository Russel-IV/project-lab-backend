package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Review
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewRepository : JpaRepository<Review, Int> {

    fun findByStayId(stayId: Int, pageable: Pageable): List<Review>

    @Query("SELECT r.rating AS rating, COUNT(r) AS count FROM Review r WHERE r.stayId = :stayId GROUP BY r.rating")
    fun countByRatingForStay(stayId: Int): List<RatingCount>
}

interface RatingCount {
    val rating: Int
    val count: Long
}
