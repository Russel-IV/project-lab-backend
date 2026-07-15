package com.team1.project_lab_backend.review.models

/**
 * No longer a JPA @Entity — Review is owned by review-service now (docs/adr/0002).
 * This is a plain DTO: the GraphQL return type for ReviewResolver/ReviewBatchResolver,
 * and the JSON shape review-service's internal REST API serializes/deserializes.
 */
data class Review(
    val id: Int,
    val text: String,
    val userId: Int,
    val stayId: Int,
    val rating: Int,
)
