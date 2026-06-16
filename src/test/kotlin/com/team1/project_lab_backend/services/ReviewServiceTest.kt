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

class ReviewServiceTest {
    private val reviewRepository = Mockito.mock(ReviewRepository::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)

    private val reviewService = ReviewService(reviewRepository, userRepository, stayRepository)

    @Test
    fun createReviewRejectsMissingUser() {
        val request = ReviewRequest(text = "Great stay", userId = 99, stayId = 1)
        Mockito.`when`(userRepository.existsById(99)).thenReturn(false)
        Mockito.`when`(stayRepository.existsById(1)).thenReturn(true)

        val exception = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createReviewReturnsPersistedReview() {
        val request = ReviewRequest(text = "Great stay", userId = 1, stayId = 2)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        val saved = Review(id = 5, text = request.text, userId = request.userId, stayId = request.stayId)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val response = reviewService.createReview(request)

        assertEquals(5, response.id)
        assertEquals("Great stay", response.text)
    }

    @Test
    fun createReviewRejectsBlankText() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "  ", userId = 1, stayId = 1))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsMissingStay() {
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.createReview(ReviewRequest(text = "Nice", userId = 1, stayId = 99))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.existsById(99)).thenReturn(false)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(1)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.updateReview(99, ReviewRequest(text = "Updated", userId = 1, stayId = 1))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateReviewReturnsUpdatedReview() {
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        Mockito.`when`(reviewRepository.existsById(5)).thenReturn(true)
        val saved = Review(id = 5, text = "Updated text", userId = 1, stayId = 2)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val result = reviewService.updateReview(5, ReviewRequest(text = "Updated text", userId = 1, stayId = 2))

        assertEquals(5, result.id)
        assertEquals("Updated text", result.text)
    }

    @Test
    fun deleteReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            reviewService.deleteReview(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteReviewInvokesRepository() {
        Mockito.`when`(reviewRepository.existsById(5)).thenReturn(true)

        reviewService.deleteReview(5)

        Mockito.verify(reviewRepository).deleteById(5)
    }
}
