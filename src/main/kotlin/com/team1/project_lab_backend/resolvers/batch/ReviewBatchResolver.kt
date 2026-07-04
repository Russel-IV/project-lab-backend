package com.team1.project_lab_backend.resolvers.batch

import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class ReviewBatchResolver(
    private val userRepository: UserRepository,
) {
    @BatchMapping
    fun user(reviews: List<Review>): Map<Review, User> {
        val ids = reviews.map { it.userId }.distinct()
        val loaded = userRepository.findAllById(ids).associateBy { it.id }
        return reviews.associateWith { loaded[it.userId]!! }
    }
}
