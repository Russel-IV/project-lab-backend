package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.repositories.ReviewRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val stayRepository: StayRepository,
) {
    @Transactional(readOnly = true)
    fun getAllReviews(page: Int = 0, size: Int = 20): List<Review> =
        reviewRepository.findAll(PageRequest.of(page, size)).content

    @Transactional
    fun createReview(request: ReviewRequest): Review {
        request.text.requireNotBlank("text")
        request.userId.requirePositive("userId")
        request.stayId.requirePositive("stayId")
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId not found")
        }
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        return reviewRepository.save(Review(text = request.text, userId = request.userId, stayId = request.stayId))
    }

    @Transactional
    fun updateReview(id: Int, request: ReviewRequest): Review {
        id.requirePositive()
        request.text.requireNotBlank("text")
        request.userId.requirePositive("userId")
        request.stayId.requirePositive("stayId")
        reviewRepository.requireExistsById(id, "review not found")
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "userId not found")
        }
        if (!stayRepository.existsById(request.stayId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId not found")
        }
        return reviewRepository.save(
            Review(id = id, text = request.text, userId = request.userId, stayId = request.stayId),
        )
    }

    @Transactional
    fun deleteReview(id: Int) {
        id.requirePositive()
        reviewRepository.requireExistsById(id, "review not found")
        reviewRepository.deleteById(id)
    }
}
