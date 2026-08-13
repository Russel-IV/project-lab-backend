package com.team1.project_lab_backend.identity.repositories

import com.team1.project_lab_backend.identity.models.VerificationToken
import com.team1.project_lab_backend.identity.models.VerificationTokenType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.Optional

interface VerificationTokenRepository : JpaRepository<VerificationToken, Int> {
    fun findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(
        token: String,
        type: VerificationTokenType,
        now: LocalDateTime,
    ): Optional<VerificationToken>

    fun deleteByUserIdAndTypeAndUsedAtIsNull(
        userId: Int,
        type: VerificationTokenType,
    )
}
