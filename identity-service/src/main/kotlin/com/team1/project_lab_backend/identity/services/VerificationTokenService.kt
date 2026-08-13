package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.VerificationToken
import com.team1.project_lab_backend.identity.models.VerificationTokenType
import com.team1.project_lab_backend.identity.repositories.VerificationTokenRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.Base64

@Service
class VerificationTokenService(
    private val verificationTokenRepository: VerificationTokenRepository,
) {
    private val secureRandom = SecureRandom()

    // Deleting prior unused tokens first means only one link is ever live at a time.
    // @Transactional is required here: the derived delete-by query below isn't
    // auto-wrapped the way save()/deleteById() are on SimpleJpaRepository.
    @Transactional
    fun issueToken(
        userId: Int,
        type: VerificationTokenType,
        ttl: Duration,
    ): String {
        verificationTokenRepository.deleteByUserIdAndTypeAndUsedAtIsNull(userId, type)
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        verificationTokenRepository.save(
            VerificationToken(
                userId = userId,
                token = token,
                type = type,
                expiresAt = LocalDateTime.now().plus(ttl),
            ),
        )
        return token
    }

    @Transactional
    fun consumeToken(
        token: String,
        type: VerificationTokenType,
    ): VerificationToken {
        val record =
            verificationTokenRepository
                .findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(token, type, LocalDateTime.now())
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid or expired token") }
        return verificationTokenRepository.save(record.copy(usedAt = LocalDateTime.now()))
    }
}
