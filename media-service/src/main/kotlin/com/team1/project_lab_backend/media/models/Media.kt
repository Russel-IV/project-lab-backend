package com.team1.project_lab_backend.media.models

import jakarta.persistence.*

enum class MediaOwnerType { STAY, ROOM, USER }

@Entity
@Table(name = "media")
data class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 10)
    val ownerType: MediaOwnerType,

    @Column(name = "owner_id", nullable = false)
    val ownerId: Int,

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    val url: String,

    @Column(name = "caption", columnDefinition = "TEXT")
    val caption: String? = null,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
)
