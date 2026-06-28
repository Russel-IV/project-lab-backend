package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.repositories.ReviewRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class ReviewServiceTest {
    private val reviewRepository = Mockito.mock(ReviewRepository::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)

    private val reviewService = ReviewService(reviewRepository, userRepository, stayRepository)

    @Test
    fun createReviewRejectsMissingUser() {
        val request = ReviewRequest(text = "Great stay", userId = 99, stayId = 1, rating = 5)
        Mockito.`when`(userRepository.existsById(99)).thenReturn(false)
        Mockito.`when`(stayRepository.existsById(1)).thenReturn(true)

        val exception = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createReviewReturnsPersistedReview() {
        val request = ReviewRequest(text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        val saved = Review(id = 5, text = request.text, userId = request.userId, stayId = request.stayId, rating = request.rating)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val response = reviewService.createReview(request)

        assertEquals(5, response.id)
        assertEquals("Great stay", response.text)
        assertEquals(4, response.rating)
    }

    @Test
    fun createReviewRejectsBlankText() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "  ", userId = 1, stayId = 1, rating = 3))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsInvalidRating() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "Nice", userId = 1, stayId = 1, rating = 6))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsZeroRating() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "Nice", userId = 1, stayId = 1, rating = 0))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsMissingStay() {
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "Nice", userId = 1, stayId = 99, rating = 3))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.findById(99)).thenReturn(Optional.empty())
        Mockito.`when`(stayRepository.existsById(1)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.updateReview(99, ReviewRequest(text = "Updated", userId = 0, stayId = 1, rating = 4), 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateReviewReturnsUpdatedReview() {
        val existing = Review(id = 5, text = "Original", userId = 1, stayId = 2, rating = 3)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        val saved = Review(id = 5, text = "Updated text", userId = 1, stayId = 2, rating = 5)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val result = reviewService.updateReview(5, ReviewRequest(text = "Updated text", userId = 0, stayId = 2, rating = 5), 1)

        assertEquals(5, result.id)
        assertEquals("Updated text", result.text)
        assertEquals(1, result.userId)
        assertEquals(5, result.rating)
    }

    @Test
    fun deleteReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.deleteReview(99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteReviewInvokesRepository() {
        val existing = Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))

        reviewService.deleteReview(5, 1)

        Mockito.verify(reviewRepository).deleteById(5)
    }
}
