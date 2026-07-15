package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.repositories.BookingRepository
import com.team1.project_lab_backend.repositories.ReviewRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.util.GraphQLBusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }

class ReviewServiceTest {
    private val reviewRepository = Mockito.mock(ReviewRepository::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)
    private val bookingRepository = Mockito.mock(BookingRepository::class.java)

    private val reviewService = ReviewService(reviewRepository, userRepository, stayRepository, bookingRepository)

    private fun stubEligible(userId: Int, stayId: Int) {
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(userId, stayId)).thenReturn(false)
        Mockito.`when`(bookingRepository.existsBookingForUserAndStayWithStatus(userId, stayId, BookingStatus.COMPLETED))
            .thenReturn(true)
    }

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
        stubEligible(1, 2)
        val saved = Review(id = 5, text = request.text, userId = request.userId, stayId = request.stayId, rating = request.rating)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val response = reviewService.createReview(request)

        assertEquals(5, response.id)
        assertEquals("Great stay", response.text)
        assertEquals(4, response.rating)
    }

    @Test
    fun createReviewRejectsAlreadyReviewed() {
        val request = ReviewRequest(text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(1, 2)).thenReturn(true)

        val ex = assertThrows(GraphQLBusinessException::class.java) {
            reviewService.createReview(request)
        }

        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        assertEquals("ALREADY_REVIEWED", ex.code)
    }

    @Test
    fun createReviewRejectsWithoutCompletedBooking() {
        val request = ReviewRequest(text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(1, 2)).thenReturn(false)
        Mockito.`when`(bookingRepository.existsBookingForUserAndStayWithStatus(1, 2, BookingStatus.COMPLETED))
            .thenReturn(false)

        val ex = assertThrows(GraphQLBusinessException::class.java) {
            reviewService.createReview(request)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("NOT_ELIGIBLE", ex.code)
    }

    @Test
    fun createReviewTrimsText() {
        val request = ReviewRequest(text = "  Great stay  ", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(userRepository.existsById(1)).thenReturn(true)
        Mockito.`when`(stayRepository.existsById(2)).thenReturn(true)
        stubEligible(1, 2)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java)))
            .thenAnswer { it.arguments[0] as Review }

        val response = reviewService.createReview(request)

        assertEquals("Great stay", response.text)
    }

    @Test
    fun getMyReviewForStayReturnsNullWhenNoneExists() {
        Mockito.`when`(reviewRepository.findByUserIdAndStayId(1, 2)).thenReturn(null)

        val result = reviewService.getMyReviewForStay(1, 2)

        assertNull(result)
    }

    @Test
    fun getMyReviewForStayReturnsExistingReview() {
        val existing = Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.findByUserIdAndStayId(1, 2)).thenReturn(existing)

        val result = reviewService.getMyReviewForStay(1, 2)

        assertEquals(5, result?.id)
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
    fun getMyReviewsDelegatesToRepositoryWithPaging() {
        val reviews = listOf(Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4))
        Mockito.`when`(reviewRepository.findByUserId(Mockito.eq(1), anyArg())).thenReturn(reviews)

        val result = reviewService.getMyReviews(1, page = 0, size = 5)

        assertEquals(listOf(5), result.map { it.id })
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
