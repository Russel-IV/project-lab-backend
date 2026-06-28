package com.team1.project_lab_backend.dto

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
