package com.team1.project_lab_backend.review.dto

import java.math.BigDecimal

data class ReviewSummary(
    val count: Int,
    val average: BigDecimal?,
    val oneStar: Int,
    val twoStar: Int,
    val threeStar: Int,
    val fourStar: Int,
    val fiveStar: Int,
)

data class CreateReviewRequest(
    val text: String,
    val userId: Int,
    val stayId: Int,
    val rating: Int,
)

data class UpdateReviewRequest(
    val text: String,
    val stayId: Int,
    val rating: Int,
    val requestingUserId: Int,
)
