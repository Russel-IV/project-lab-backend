package com.team1.project_lab_backend.review.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "favorite")
data class Favorite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    // No FK to "user" — dropped per docs/adr/0011; existence is implied by a valid JWT
    // at the Gateway, which is the only caller of this internal API.
    @Column(nullable = false, name = "user_id")
    val userId: Int,
    // No FK to "stay" — dropped per docs/adr/0011; mirrors Review.kt's stayId.
    @Column(nullable = false, name = "stay_id")
    val stayId: Int,
)
