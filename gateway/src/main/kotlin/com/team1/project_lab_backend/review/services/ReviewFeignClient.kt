package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.models.Review
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Resolves via Eureka to review-service's internal REST API (docs/adr/0005,
 * docs/adr/0008). Mirrors ReviewController one-to-one — this interface and that
 * controller are two halves of one contract that must be kept in sync by hand, since
 * there's no shared library between the two modules.
 */
@FeignClient(name = "review-service")
interface ReviewFeignClient {

    @GetMapping("/internal/reviews")
    fun list(
        @RequestParam(required = false) stayId: Int?,
        @RequestParam(required = false) userId: Int?,
        @RequestParam(required = false) ids: List<Int>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<Review>

    @GetMapping("/internal/reviews/summary")
    fun summary(@RequestParam stayId: Int): ReviewSummary

    @GetMapping("/internal/reviews/mine")
    fun mine(@RequestParam userId: Int, @RequestParam stayId: Int): Review

    @PostMapping("/internal/reviews")
    fun create(@RequestBody request: CreateReviewRequest): Review

    @PatchMapping("/internal/reviews/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: UpdateReviewRequest): Review

    @DeleteMapping("/internal/reviews/{id}")
    fun delete(@PathVariable id: Int, @RequestParam requestingUserId: Int)
}

data class CreateReviewRequest(val text: String, val userId: Int, val stayId: Int, val rating: Int)
data class UpdateReviewRequest(val text: String, val stayId: Int, val rating: Int, val requestingUserId: Int)
