package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.dto.ReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import com.team1.project_lab_backend.review.repositories.ReviewRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.GraphQLBusinessException
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val stayRepository: StayRepository,
    private val bookingRepository: BookingRepository,
) {
    @Transactional(readOnly = true)
    fun getAllReviews(page: Int = 0, size: Int = 20): List<Review> =
        reviewRepository.findAll(PageRequest.of(page, size)).content

    @Transactional(readOnly = true)
    fun getReviewsByStay(stayId: Int, page: Int = 0, size: Int = 20): List<Review> {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return reviewRepository.findByStayId(stayId, PageRequest.of(page, size))
    }

    @Transactional(readOnly = true)
    fun getMyReviews(userId: Int, page: Int = 0, size: Int = 20): List<Review> {
        userId.requirePositive("userId")
        return reviewRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    @Transactional(readOnly = true)
    fun getReviewSummary(stayId: Int): ReviewSummary {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        val counts = reviewRepository.countByRatingForStay(stayId).associate { it.rating to it.count }
        val total = counts.values.sum()
        val average = if (total == 0L) {
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
    fun getMyReviewForStay(userId: Int, stayId: Int): Review? {
        stayId.requirePositive("stayId")
        return reviewRepository.findByUserIdAndStayId(userId, stayId)
    }

    @Transactional
    fun createReview(request: ReviewRequest): Review {
        val text = request.text.trim()
        text.requireNotBlank("text")
        if (request.rating !in 1..5)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5")
        request.userId.requirePositive("userId")
        request.stayId.requirePositive("stayId")
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId not found")
        }
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        if (reviewRepository.existsByUserIdAndStayId(request.userId, request.stayId)) {
            throw GraphQLBusinessException(
                "ALREADY_REVIEWED",
                HttpStatus.CONFLICT,
                "you have already reviewed this stay",
            )
        }
        if (!bookingRepository.existsBookingForUserAndStayWithStatus(request.userId, request.stayId, BookingStatus.COMPLETED)) {
            throw GraphQLBusinessException(
                "NOT_ELIGIBLE",
                HttpStatus.FORBIDDEN,
                "you must have a completed booking for this stay to review it",
            )
        }
        return reviewRepository.save(
            Review(text = text, userId = request.userId, stayId = request.stayId, rating = request.rating),
        )
    }

    @Transactional
    fun updateReview(id: Int, request: ReviewRequest, requestingUserId: Int): Review {
        id.requirePositive()
        request.text.requireNotBlank("text")
        if (request.rating !in 1..5)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5")
        request.stayId.requirePositive("stayId")
        val existing = reviewRepository.findById(id).orNotFound("review not found")
        if (existing.userId != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        return reviewRepository.save(
            Review(id = id, text = request.text, userId = existing.userId, stayId = request.stayId, rating = request.rating),
        )
    }

    @Transactional
    fun deleteReview(id: Int, requestingUserId: Int) {
        id.requirePositive()
        val review = reviewRepository.findById(id).orNotFound("review not found")
        if (review.userId != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        reviewRepository.deleteById(id)
    }
}
