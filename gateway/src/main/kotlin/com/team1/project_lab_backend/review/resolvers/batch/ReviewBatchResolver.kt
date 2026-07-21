package com.team1.project_lab_backend.review.resolvers.batch

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserFeignClient
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.services.StayFeignClient
import com.team1.project_lab_backend.review.models.Review
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class ReviewBatchResolver(
    private val userFeignClient: UserFeignClient,
    private val stayFeignClient: StayFeignClient,
) {
    @BatchMapping
    suspend fun user(reviews: List<Review>): Map<Review, User> {
        val ids = reviews.map { it.userId }.distinct()
        val loaded = userFeignClient.list(ids).associateBy { it.id }
        return reviews.associateWith { loaded[it.userId]!! }
    }

    @BatchMapping
    suspend fun stay(reviews: List<Review>): Map<Review, Stay> {
        val ids = reviews.map { it.stayId }.distinct()
        val loaded = stayFeignClient.list(ids).associateBy { it.id }
        return reviews.associateWith { loaded[it.stayId]!! }
    }
}
