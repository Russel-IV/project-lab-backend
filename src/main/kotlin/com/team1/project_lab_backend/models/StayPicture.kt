package com.team1.project_lab_backend.models

import jakarta.persistence.*

@Entity
@Table(name = "stay_picture")
data class StayPicture(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "stay_id", nullable = false)
    val stayId: Int,

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    val url: String,

    @Column(name = "caption", columnDefinition = "TEXT")
    val caption: String? = null,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0
)
