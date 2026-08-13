package com.team1.project_lab_backend.identity.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class VerificationTokenType {
    PASSWORD_RESET,
    EMAIL_CONFIRMATION,
}

@Entity
@Table(name = "verification_token")
data class VerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Int,
    @Column(name = "token", nullable = false, length = 128)
    val token: String,
    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: VerificationTokenType,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    @Column(name = "used_at")
    val usedAt: LocalDateTime? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
