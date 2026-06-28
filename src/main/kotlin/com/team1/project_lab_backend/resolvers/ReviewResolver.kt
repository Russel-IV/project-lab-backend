package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.services.ReviewService
import com.team1.project_lab_backend.util.requireAuthenticated
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
    ): List<Review> = reviewService.getAllReviews(page ?: 0, size ?: 20)

    @MutationMapping
    fun createReview(@Argument input: CreateReviewInput): Review {
        val currentUser = requireAuthenticated()
        return reviewService.createReview(ReviewRequest(text = input.text, userId = currentUser.id, stayId = input.stayId))
    }

    @MutationMapping
    fun updateReview(@Argument id: Int, @Argument input: UpdateReviewInput): Review {
        requireAuthenticated()
        return reviewService.updateReview(id, ReviewRequest(text = input.text, userId = 0, stayId = input.stayId))
    }

    @MutationMapping
    fun deleteReview(@Argument id: Int): Boolean {
        requireAuthenticated()
        reviewService.deleteReview(id)
        return true
    }
}

data class CreateReviewInput(val text: String, val stayId: Int)
data class UpdateReviewInput(val text: String, val stayId: Int)
