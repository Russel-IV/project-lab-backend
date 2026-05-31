package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.dto.ReviewResponse
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.repositories.ReviewRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val stayRepository: StayRepository
) {
    @Transactional(readOnly = true)
    fun getAllReviews(): List<ReviewResponse> =
        reviewRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createReview(request: ReviewRequest): ReviewResponse {
        if (request.text.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank")
        }
        if (request.userId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be positive")
        }
        if (request.stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId not found")
        }
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        val review = Review(text = request.text, userId = request.userId, stayId = request.stayId)
        return reviewRepository.save(review).toResponse()
    }

    @Transactional
    fun updateReview(id: Int, request: ReviewRequest): ReviewResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.text.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank")
        }
        if (request.userId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be positive")
        }
        if (request.stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!reviewRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        }
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId not found")
        }
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        val review = Review(id = id, text = request.text, userId = request.userId, stayId = request.stayId)
        return reviewRepository.save(review).toResponse()
    }

    @Transactional
    fun deleteReview(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!reviewRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "review not found")
        }
        reviewRepository.deleteById(id)
    }

}

private fun Review.toResponse(): ReviewResponse =
    ReviewResponse(id = requireNotNull(id), text = text, userId = userId, stayId = stayId)
