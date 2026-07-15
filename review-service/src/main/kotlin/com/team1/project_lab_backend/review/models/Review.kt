package com.team1.project_lab_backend.review.models

import jakarta.persistence.*

@Entity
@Table(name = "review")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String,

    // No FK to "user" — dropped per docs/adr/0011; existence is implied by a valid JWT
    // at the Gateway, which is the only caller of this internal API.
    @Column(nullable = false, name = "user_id")
    val userId: Int,

    // No FK to "stay" — dropped per docs/adr/0011; the Gateway validates stayId against
    // its still-local Inventory data before calling this service (see ReviewService.kt
    // in the gateway module) until Inventory is itself extracted.
    @Column(nullable = false, name = "stay_id")
    val stayId: Int,

    @Column(nullable = false)
    val rating: Int = 0,
)
