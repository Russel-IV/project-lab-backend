package com.team1.project_lab_backend.models

import jakarta.persistence.*

@Entity
@Table(name = "room_picture")
data class RoomPicture(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "room_id", nullable = false)
    val roomId: Int,

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    val url: String,

    @Column(name = "caption", columnDefinition = "TEXT")
    val caption: String? = null,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0
)
