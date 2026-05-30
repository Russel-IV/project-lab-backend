package com.team1.project_lab_backend.models

import jakarta.persistence.*

@Entity
@Table(name = "review")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String,

    @Column(nullable = false, name = "user_id")
    val userId: Int,

    @Column(nullable = false, name = "stay_id")
    val stayId: Int,
)
