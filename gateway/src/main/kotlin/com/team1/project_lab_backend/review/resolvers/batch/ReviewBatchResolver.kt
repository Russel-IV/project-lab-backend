package com.team1.project_lab_backend.review.resolvers.batch

import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.identity.repositories.UserRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class ReviewBatchResolver(
    private val userRepository: UserRepository,
    private val stayRepository: StayRepository,
) {
    @BatchMapping
    fun user(reviews: List<Review>): Map<Review, User> {
        val ids = reviews.map { it.userId }.distinct()
        val loaded = userRepository.findAllById(ids).associateBy { it.id }
        return reviews.associateWith { loaded[it.userId]!! }
    }

    @BatchMapping
    fun stay(reviews: List<Review>): Map<Review, Stay> {
        val ids = reviews.map { it.stayId }.distinct()
        val loaded = stayRepository.findAllById(ids).associateBy { it.id }
        return reviews.associateWith { loaded[it.stayId]!! }
    }
}
