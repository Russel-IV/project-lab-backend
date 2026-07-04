package com.team1.project_lab_backend.dto

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

data class ReviewRequest(
    val text: String,
    val userId: Int,
    val stayId: Int,
    val rating: Int,
)

data class ReviewResponse(
    val id: Int,
    val text: String,
    val userId: Int,
    val stayId: Int,
    val rating: Int,
)
