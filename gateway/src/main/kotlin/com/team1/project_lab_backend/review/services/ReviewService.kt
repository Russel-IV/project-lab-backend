package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.booking.models.BookingStatus
import com.team1.project_lab_backend.booking.repositories.BookingRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.review.dto.ReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.util.GraphQLBusinessException
import com.team1.project_lab_backend.util.feignErrorMessage
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requirePositive
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): the actual Review data and its own business
 * rules (text/rating format, duplicate-review check) now live in review-service,
 * reached via reviewFeignClient. What's still here is exactly the cross-domain
 * validation review-service can't do itself, because Inventory and Booking haven't
 * been extracted yet and review-service has no way to reach their data:
 *  - stayId existence — trusted from local Inventory data (stayRepository) until
 *    Inventory is extracted (docs/adr/0011), at which point this becomes a Feign call.
 *  - booking-completion eligibility — trusted from local Booking data
 *    (bookingRepository) until Booking is extracted (Phase 6).
 * userId existence is no longer checked at all (not deferred — dropped for good, per
 * docs/adr/0011): it's always the JWT-authenticated caller's own id, and a valid JWT
 * already implies a real user.
 */
@Service
class ReviewService(
    private val reviewFeignClient: ReviewFeignClient,
    private val stayRepository: StayRepository,
    private val bookingRepository: BookingRepository,
) {
    fun getAllReviews(page: Int = 0, size: Int = 20): List<Review> =
        reviewFeignClient.list(stayId = null, userId = null, ids = null, page = page, size = size)

    fun getReviewsByStay(stayId: Int, page: Int = 0, size: Int = 20): List<Review> {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return reviewFeignClient.list(stayId = stayId, userId = null, ids = null, page = page, size = size)
    }

    fun getMyReviews(userId: Int, page: Int = 0, size: Int = 20): List<Review> {
        userId.requirePositive("userId")
        return reviewFeignClient.list(stayId = null, userId = userId, ids = null, page = page, size = size)
    }

    fun getReviewSummary(stayId: Int): ReviewSummary {
        stayId.requirePositive("stayId")
        stayRepository.requireExistsById(stayId, "stay not found")
        return reviewFeignClient.summary(stayId)
    }

    fun getMyReviewForStay(userId: Int, stayId: Int): Review? {
        stayId.requirePositive("stayId")
        return try {
            reviewFeignClient.mine(userId, stayId)
        } catch (e: FeignException.NotFound) {
            null
        }
    }

    fun createReview(request: ReviewRequest): Review {
        request.stayId.requirePositive("stayId")
        stayRepository.requireExistsById(request.stayId, "stay not found")
        if (!bookingRepository.existsBookingForUserAndStayWithStatus(request.userId, request.stayId, BookingStatus.COMPLETED)) {
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
        } catch (e: FeignException.Conflict) {
            throw GraphQLBusinessException("ALREADY_REVIEWED", HttpStatus.CONFLICT, "you have already reviewed this stay")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid review")
        }
    }

    fun updateReview(id: Int, request: ReviewRequest, requestingUserId: Int): Review {
        request.stayId.requirePositive("stayId")
        stayRepository.requireExistsById(request.stayId, "stay not found")
        return try {
            reviewFeignClient.update(
                id,
                UpdateReviewRequest(text = request.text, stayId = request.stayId, rating = request.rating, requestingUserId = requestingUserId),
            )
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid review")
        }
    }

    fun deleteReview(id: Int, requestingUserId: Int) {
        try {
            reviewFeignClient.delete(id, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
