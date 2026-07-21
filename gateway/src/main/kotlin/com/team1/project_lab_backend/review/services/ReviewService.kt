package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.booking.services.BookingService
import com.team1.project_lab_backend.inventory.services.StayFeignClient
import com.team1.project_lab_backend.review.dto.ReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.util.GraphQLBusinessException
import com.team1.project_lab_backend.util.webClientErrorMessage
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): the actual Review data and its own business
 * rules (text/rating format, duplicate-review check) now live in review-service,
 * reached via reviewFeignClient. What's still here is exactly the cross-domain
 * validation review-service can't do itself, because Booking hasn't been extracted
 * yet and review-service has no way to reach its data:
 *  - stayId existence — now a call to inventory-service (docs/adr/0011,
 *    Phase 5), same pattern as every other cross-service existence check in this
 *    migration.
 *  - booking-completion eligibility — delegates to BookingService (still local until
 *    Phase 6) rather than the repository directly, since that check now needs its own
 *    round trip to inventory-service (Room.stayId) that BookingService already
 *    implements for myBookingStatusForStay.
 * userId existence is no longer checked at all (not deferred — dropped for good, per
 * docs/adr/0011): it's always the JWT-authenticated caller's own id, and a valid JWT
 * already implies a real user.
 */
@Service
class ReviewService(
    private val reviewFeignClient: ReviewFeignClient,
    private val stayFeignClient: StayFeignClient,
    private val bookingService: BookingService,
) {
    suspend fun getAllReviews(
        page: Int = 0,
        size: Int = 20,
    ): List<Review> = reviewFeignClient.list(stayId = null, userId = null, ids = null, page = page, size = size)

    suspend fun getReviewsByStay(
        stayId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Review> {
        stayId.requirePositive("stayId")
        requireStayExists(stayId)
        return reviewFeignClient.list(stayId = stayId, userId = null, ids = null, page = page, size = size)
    }

    suspend fun getMyReviews(
        userId: Int,
        page: Int = 0,
        size: Int = 20,
    ): List<Review> {
        userId.requirePositive("userId")
        return reviewFeignClient.list(stayId = null, userId = userId, ids = null, page = page, size = size)
    }

    suspend fun getReviewSummary(stayId: Int): ReviewSummary {
        stayId.requirePositive("stayId")
        requireStayExists(stayId)
        return reviewFeignClient.summary(stayId)
    }

    suspend fun getMyReviewForStay(
        userId: Int,
        stayId: Int,
    ): Review? {
        stayId.requirePositive("stayId")
        return try {
            reviewFeignClient.mine(userId, stayId)
        } catch (e: WebClientResponseException.NotFound) {
            null
        }
    }

    suspend fun createReview(request: ReviewRequest): Review {
        request.stayId.requirePositive("stayId")
        requireStayExists(request.stayId)
        if (!bookingService.hasCompletedBookingForStay(request.userId, request.stayId)) {
            throw GraphQLBusinessException(
                "NOT_ELIGIBLE",
                HttpStatus.FORBIDDEN,
                "you must have a completed booking for this stay to review it",
            )
        }
        return try {
            reviewFeignClient.create(
                CreateReviewRequest(text = request.text, userId = request.userId, stayId = request.stayId, rating = request.rating),
            )
        } catch (e: WebClientResponseException.Conflict) {
            throw GraphQLBusinessException("ALREADY_REVIEWED", HttpStatus.CONFLICT, "you have already reviewed this stay")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid review")
        }
    }

    suspend fun updateReview(
        id: Int,
        request: ReviewRequest,
        requestingUserId: Int,
    ): Review {
        request.stayId.requirePositive("stayId")
        requireStayExists(request.stayId)
        return try {
            reviewFeignClient.update(
                id,
                UpdateReviewRequest(
                    text = request.text,
                    stayId = request.stayId,
                    rating = request.rating,
                    requestingUserId = requestingUserId,
                ),
            )
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid review")
        }
    }

    suspend fun deleteReview(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            reviewFeignClient.delete(id, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }

    private suspend fun requireStayExists(stayId: Int) {
        try {
            stayFeignClient.get(stayId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
    }
}
