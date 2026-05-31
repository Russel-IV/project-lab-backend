package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.ReviewRequest
import com.team1.project_lab_backend.dto.ReviewResponse
import com.team1.project_lab_backend.services.ReviewService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    @GetMapping
    fun getAllReviews(): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(reviewService.getAllReviews())

    @PostMapping
    fun createReview(@RequestBody review: ReviewRequest): ResponseEntity<ReviewResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(review))

    @PutMapping("/{id}")
    fun updateReview(@PathVariable id: Int, @RequestBody review: ReviewRequest): ResponseEntity<ReviewResponse> =
        ResponseEntity.ok(reviewService.updateReview(id, review))

    @DeleteMapping("/{id}")
    fun deleteReview(@PathVariable id: Int): ResponseEntity<Unit> =
        reviewService.deleteReview(id).let { ResponseEntity.noContent().build() }
}
