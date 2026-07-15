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

    @Column(nullable = false, name = "user_id")
    val userId: Int,

    @Column(nullable = false, name = "stay_id")
    val stayId: Int,

    @Column(nullable = false)
    val rating: Int = 0,
)
