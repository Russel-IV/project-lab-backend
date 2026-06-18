package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.services.ReviewService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ReviewResolver(private val reviewService: ReviewService) {

    @QueryMapping
    fun reviews(
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Review> = reviewService.getAllReviews().let { all ->
        val p = page ?: 0
        val s = size ?: 20
        all.drop(p * s).take(s)
    }

    @MutationMapping
    fun createReview(@Argument input: CreateReviewInput): Review =
        reviewService.createReview(ReviewRequest(text = input.text, userId = input.userId, stayId = input.stayId))

    @MutationMapping
    fun updateReview(@Argument id: Int, @Argument input: UpdateReviewInput): Review =
        reviewService.updateReview(id, ReviewRequest(text = input.text, userId = input.userId, stayId = input.stayId))

    @MutationMapping
    fun deleteReview(@Argument id: Int): Boolean {
        reviewService.deleteReview(id)
        return true
    }
}

data class CreateReviewInput(val text: String, val userId: Int, val stayId: Int)
data class UpdateReviewInput(val text: String, val userId: Int, val stayId: Int)
