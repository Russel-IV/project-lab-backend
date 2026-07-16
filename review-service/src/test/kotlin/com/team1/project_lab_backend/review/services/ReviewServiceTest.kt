package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.dto.CreateReviewRequest
import com.team1.project_lab_backend.review.dto.UpdateReviewRequest
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.review.repositories.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

class ReviewServiceTest {
    private val reviewRepository = Mockito.mock(ReviewRepository::class.java)
    private val reviewService = ReviewService(reviewRepository)

    // ---- createReview ----

    @Test
    fun createReviewReturnsPersistedReview() {
        val request = CreateReviewRequest(text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(1, 2)).thenReturn(false)
        val saved = Review(id = 5, text = request.text, userId = request.userId, stayId = request.stayId, rating = request.rating)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val response = reviewService.createReview(request)

        assertEquals(5, response.id)
        assertEquals("Great stay", response.text)
        assertEquals(4, response.rating)
    }

    @Test
    fun createReviewRejectsAlreadyReviewed() {
        val request = CreateReviewRequest(text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(1, 2)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) { reviewService.createReview(request) }

        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun createReviewTrimsText() {
        val request = CreateReviewRequest(text = "  Great stay  ", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.existsByUserIdAndStayId(1, 2)).thenReturn(false)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenAnswer { it.arguments[0] as Review }

        val response = reviewService.createReview(request)

        assertEquals("Great stay", response.text)
    }

    @Test
    fun createReviewRejectsBlankText() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                reviewService.createReview(CreateReviewRequest(text = "  ", userId = 1, stayId = 1, rating = 3))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsInvalidRating() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                reviewService.createReview(CreateReviewRequest(text = "Nice", userId = 1, stayId = 1, rating = 6))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createReviewRejectsZeroRating() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                reviewService.createReview(CreateReviewRequest(text = "Nice", userId = 1, stayId = 1, rating = 0))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updateReview ----

    @Test
    fun updateReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.findById(99)).thenReturn(Optional.empty())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                reviewService.updateReview(99, UpdateReviewRequest(text = "Updated", stayId = 1, rating = 4, requestingUserId = 1))
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateReviewRejectsWhenNotOwner() {
        val existing = Review(id = 5, text = "Original", userId = 1, stayId = 2, rating = 3)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                reviewService.updateReview(5, UpdateReviewRequest(text = "Updated", stayId = 2, rating = 4, requestingUserId = 99))
            }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun updateReviewReturnsUpdatedReview() {
        val existing = Review(id = 5, text = "Original", userId = 1, stayId = 2, rating = 3)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))
        val saved = Review(id = 5, text = "Updated text", userId = 1, stayId = 2, rating = 5)
        Mockito.`when`(reviewRepository.save(Mockito.any(Review::class.java))).thenReturn(saved)

        val result = reviewService.updateReview(5, UpdateReviewRequest(text = "Updated text", stayId = 2, rating = 5, requestingUserId = 1))

        assertEquals(5, result.id)
        assertEquals("Updated text", result.text)
        assertEquals(1, result.userId)
        assertEquals(5, result.rating)
    }

    // ---- deleteReview ----

    @Test
    fun deleteReviewReturnsNotFoundWhenMissing() {
        Mockito.`when`(reviewRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) { reviewService.deleteReview(99, 1) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteReviewRejectsWhenNotOwner() {
        val existing = Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))

        val ex = assertThrows(ResponseStatusException::class.java) { reviewService.deleteReview(5, 99) }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun deleteReviewInvokesRepository() {
        val existing = Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.findById(5)).thenReturn(Optional.of(existing))

        reviewService.deleteReview(5, 1)

        Mockito.verify(reviewRepository).deleteById(5)
    }

    // ---- reads ----

    @Test
    fun getMyReviewForStayReturnsNullWhenNoneExists() {
        Mockito.`when`(reviewRepository.findByUserIdAndStayId(1, 2)).thenReturn(null)

        assertNull(reviewService.getMyReviewForStay(1, 2))
    }

    @Test
    fun getMyReviewForStayReturnsExistingReview() {
        val existing = Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4)
        Mockito.`when`(reviewRepository.findByUserIdAndStayId(1, 2)).thenReturn(existing)

        assertEquals(5, reviewService.getMyReviewForStay(1, 2)?.id)
    }

    @Test
    fun getMyReviewsDelegatesToRepositoryWithPaging() {
        val reviews = listOf(Review(id = 5, text = "Great stay", userId = 1, stayId = 2, rating = 4))
        Mockito.`when`(reviewRepository.findByUserId(Mockito.eq(1), anyArg())).thenReturn(reviews)

        val result = reviewService.getMyReviews(1, page = 0, size = 5)

        assertEquals(listOf(5), result.map { it.id })
    }

    @Test
    fun findByIdsDelegatesToRepository() {
        val reviews =
            listOf(
                Review(id = 1, text = "A", userId = 1, stayId = 1, rating = 5),
                Review(id = 2, text = "B", userId = 2, stayId = 1, rating = 3),
            )
        Mockito.`when`(reviewRepository.findAllById(listOf(1, 2))).thenReturn(reviews)

        val result = reviewService.findByIds(listOf(1, 2))

        assertEquals(listOf(1, 2), result.map { it.id })
    }
}
