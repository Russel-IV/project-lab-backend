package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.dto.CreateReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.dto.UpdateReviewRequest
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.review.repositories.ReviewRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Optional

/**
 * Stay-existence and booking-eligibility checks that lived here before extraction are
 * gone, not just moved — this service has no access to Inventory's or Booking's data.
 * The Gateway's (still-local, until their own extraction phases) ReviewService shim
 * performs those checks before ever calling this service's create endpoint. See
 * gateway's review/services/ReviewService.kt.
 */
@Service
class ReviewService(private val reviewRepository: ReviewRepository) {
    @Transactional(readOnly = true)
    fun getAllReviews(
        page: Int = 0,
        size: Int = 20,
    ): List<Review> = reviewRepository.findAll(PageRequest.of(page, size)).content

    @Transactional(readOnly = true)
    fun getReviewsByStay(
        stayId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Review> = reviewRepository.findByStayId(stayId, PageRequest.of(page, size))

    @Transactional(readOnly = true)
    fun getMyReviews(
        userId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Review> = reviewRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))

    @Transactional(readOnly = true)
    fun getReviewSummary(stayId: Int): ReviewSummary {
        val counts = reviewRepository.countByRatingForStay(stayId).associate { it.rating to it.count }
        val total = counts.values.sum()
        val average =
            if (total == 0L) {
                null
            } else {
                val weightedSum = counts.entries.sumOf { (rating, count) -> rating.toLong() * count }
                BigDecimal(weightedSum).divide(BigDecimal(total), 2, RoundingMode.HALF_UP)
            }
        return ReviewSummary(
            count = total.toInt(),
            average = average,
            oneStar = (counts[1] ?: 0L).toInt(),
            twoStar = (counts[2] ?: 0L).toInt(),
            threeStar = (counts[3] ?: 0L).toInt(),
            fourStar = (counts[4] ?: 0L).toInt(),
            fiveStar = (counts[5] ?: 0L).toInt(),
        )
    }

    @Transactional(readOnly = true)
    fun getMyReviewForStay(
        userId: Int,
        stayId: Int,
    ): Review? = reviewRepository.findByUserIdAndStayId(userId, stayId)

    @Transactional(readOnly = true)
    fun findByIds(ids: List<Int>): List<Review> = reviewRepository.findAllById(ids)

    @Transactional
    fun createReview(request: CreateReviewRequest): Review {
        val text = request.text.trim()
        if (text.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank")
        if (request.rating !in 1..5) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5")
        if (reviewRepository.existsByUserIdAndStayId(request.userId, request.stayId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "you have already reviewed this stay")
        }
        return reviewRepository.save(
            Review(text = text, userId = request.userId, stayId = request.stayId, rating = request.rating),
        )
    }

    @Transactional
    fun updateReview(
        id: Int,
        request: UpdateReviewRequest,
    ): Review {
        val text = request.text.trim()
        if (text.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank")
        if (request.rating !in 1..5) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5")
        val existing = orNotFound(reviewRepository.findById(id))
        if (existing.userId != request.requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        return reviewRepository.save(
            Review(id = id, text = text, userId = existing.userId, stayId = request.stayId, rating = request.rating),
        )
    }

    @Transactional
    fun deleteReview(
        id: Int,
        requestingUserId: Int,
    ) {
        val review = orNotFound(reviewRepository.findById(id))
        if (review.userId != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        reviewRepository.deleteById(id)
    }

    private fun orNotFound(review: Optional<Review>): Review =
        review.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "review not found") }
}
