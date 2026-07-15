package com.team1.project_lab_backend.review.controllers

import com.team1.project_lab_backend.review.dto.CreateReviewRequest
import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.dto.UpdateReviewRequest
import com.team1.project_lab_backend.review.models.Review
import com.team1.project_lab_backend.review.services.ReviewService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Internal-only API (docs/adr/0005) — the Gateway's ReviewFeignClient is the only
 * caller. Not part of the public contract, so the shape here is whatever's convenient
 * for that one caller, not a versioned public REST API.
 */
@RestController
@RequestMapping("/internal/reviews")
class ReviewController(private val reviewService: ReviewService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) stayId: Int?,
        @RequestParam(required = false) userId: Int?,
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<Review> = when {
        ids != null -> reviewService.findByIds(ids)
        stayId != null -> reviewService.getReviewsByStay(stayId, page, size)
        userId != null -> reviewService.getMyReviews(userId, page, size)
        else -> reviewService.getAllReviews(page, size)
    }

    @GetMapping("/summary")
    fun summary(@RequestParam stayId: Int): ReviewSummary = reviewService.getReviewSummary(stayId)

    @GetMapping("/mine")
    fun mine(@RequestParam userId: Int, @RequestParam stayId: Int): Review =
        reviewService.getMyReviewForStay(userId, stayId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "no review for this user/stay")

    @PostMapping
    fun create(@RequestBody request: CreateReviewRequest): Review = reviewService.createReview(request)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: UpdateReviewRequest): Review =
        reviewService.updateReview(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, @RequestParam requestingUserId: Int): ResponseEntity<Void> {
        reviewService.deleteReview(id, requestingUserId)
        return ResponseEntity.noContent().build()
    }
}
