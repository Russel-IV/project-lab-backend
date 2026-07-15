package com.team1.project_lab_backend.review.resolvers

import com.team1.project_lab_backend.review.dto.ReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.review.services.ReviewService
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

    @QueryMapping
    fun reviewsByStay(
        @Argument stayId: Int,
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Review> = reviewService.getReviewsByStay(stayId, page ?: 0, size ?: 20)

    @QueryMapping
    fun reviewSummary(@Argument stayId: Int): ReviewSummary = reviewService.getReviewSummary(stayId)

    @QueryMapping
    fun myReviewForStay(@Argument stayId: Int): Review? {
        val currentUser = requireAuthenticated()
        return reviewService.getMyReviewForStay(currentUser.id, stayId)
    }

    @QueryMapping
    fun myReviews(@Argument page: Int?, @Argument size: Int?): List<Review> {
        val currentUser = requireAuthenticated()
        return reviewService.getMyReviews(currentUser.id, page ?: 0, size ?: 20)
    }

    @MutationMapping
    fun createReview(@Argument input: CreateReviewInput): Review {
        val currentUser = requireAuthenticated()
        return reviewService.createReview(ReviewRequest(text = input.text, userId = currentUser.id, stayId = input.stayId, rating = input.rating))
    }

    @MutationMapping
    fun updateReview(@Argument id: Int, @Argument input: UpdateReviewInput): Review {
        val currentUser = requireAuthenticated()
        return reviewService.updateReview(id, ReviewRequest(text = input.text, userId = 0, stayId = input.stayId, rating = input.rating), currentUser.id)
    }

    @MutationMapping
    fun deleteReview(@Argument id: Int): Boolean {
        val currentUser = requireAuthenticated()
        reviewService.deleteReview(id, currentUser.id)
        return true
    }
}

data class CreateReviewInput(val text: String, val rating: Int, val stayId: Int)
data class UpdateReviewInput(val text: String, val rating: Int, val stayId: Int)
