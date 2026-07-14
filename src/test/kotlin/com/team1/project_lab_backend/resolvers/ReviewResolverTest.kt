package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.ReviewSummary
import com.team1.project_lab_backend.models.Review
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.services.ReviewService
import com.team1.project_lab_backend.util.GraphQLBusinessException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T { Mockito.any<T>(); return null as T }
private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class ReviewResolverTest {

    private val reviewService = Mockito.mock(ReviewService::class.java)
    private val resolver = ReviewResolver(reviewService)

    private val authenticatedUser = User(id = 1, name = "Alice")

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun authenticateAs(user: User) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, emptyList())
    }

    private fun sampleReview(id: Int = 1, stayId: Int = 2) =
        Review(id = id, text = "Great stay", userId = 1, stayId = stayId, rating = 5)

    // ---- queries ----

    @Test
    fun reviewsByStayDelegatesToService() {
        Mockito.`when`(reviewService.getReviewsByStay(2, 0, 20)).thenReturn(listOf(sampleReview(1), sampleReview(2)))

        val result = resolver.reviewsByStay(stayId = 2, page = null, size = null)

        assertEquals(listOf(1, 2), result.map { it.id })
    }

    @Test
    fun reviewSummaryDelegatesToService() {
        val summary = ReviewSummary(count = 1, average = null, oneStar = 0, twoStar = 0, threeStar = 0, fourStar = 0, fiveStar = 1)
        Mockito.`when`(reviewService.getReviewSummary(2)).thenReturn(summary)

        val result = resolver.reviewSummary(2)

        assertEquals(1, result.count)
    }

    @Test
    fun myReviewForStayDelegatesToService() {
        authenticateAs(authenticatedUser)
        Mockito.`when`(reviewService.getMyReviewForStay(1, 2)).thenReturn(sampleReview())

        val result = resolver.myReviewForStay(2)

        assertEquals(1, result?.id)
    }

    @Test
    fun myReviewForStayReturnsNullWhenNoneExists() {
        authenticateAs(authenticatedUser)
        Mockito.`when`(reviewService.getMyReviewForStay(1, 2)).thenReturn(null)

        val result = resolver.myReviewForStay(2)

        assertNull(result)
    }

    @Test
    fun myReviewForStayRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.myReviewForStay(2) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    // ---- mutations ----

    @Test
    fun createReviewDelegatesToService() {
        authenticateAs(authenticatedUser)
        Mockito.`when`(reviewService.createReview(anyArg())).thenReturn(sampleReview(10))

        val result = resolver.createReview(CreateReviewInput(text = "Great stay", rating = 5, stayId = 2))

        assertEquals(10, result.id)
        Mockito.verify(reviewService).createReview(anyArg())
    }

    @Test
    fun createReviewRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            resolver.createReview(CreateReviewInput(text = "Great stay", rating = 5, stayId = 2))
        }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun createReviewPropagatesNotEligibleError() {
        authenticateAs(authenticatedUser)
        Mockito.`when`(reviewService.createReview(anyArg())).thenThrow(
            GraphQLBusinessException("NOT_ELIGIBLE", HttpStatus.FORBIDDEN, "you must have a completed booking for this stay to review it"),
        )

        val ex = assertThrows(GraphQLBusinessException::class.java) {
            resolver.createReview(CreateReviewInput(text = "Great stay", rating = 5, stayId = 2))
        }
        assertEquals("NOT_ELIGIBLE", ex.code)
    }

    @Test
    fun deleteReviewReturnsTrueOnSuccess() {
        authenticateAs(authenticatedUser)
        Mockito.doNothing().`when`(reviewService).deleteReview(eqArg(1), eqArg(1))

        val result = resolver.deleteReview(1)

        assertEquals(true, result)
        Mockito.verify(reviewService).deleteReview(eqArg(1), eqArg(1))
    }
}
