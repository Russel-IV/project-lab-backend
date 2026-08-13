package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.VerificationToken
import com.team1.project_lab_backend.identity.models.VerificationTokenType
import com.team1.project_lab_backend.identity.repositories.VerificationTokenRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

// Mockito.eq()/any() return null internally, which crashes when passed straight into a
// Kotlin non-null-typed parameter — this local wrapper is the standard workaround
// (Elvis-falls-back-to-the-real-value, so the matcher stack push still happens correctly).
private fun <T> eqK(value: T): T = Mockito.eq(value) ?: value

private fun <T> anyK(type: Class<T>): T = Mockito.any(type)

class VerificationTokenServiceTest {
    private val verificationTokenRepository = Mockito.mock(VerificationTokenRepository::class.java)
    private val service = VerificationTokenService(verificationTokenRepository)

    @Test
    fun issueTokenDeletesPriorUnusedTokensThenSavesNewOne() {
        Mockito.`when`(verificationTokenRepository.save(Mockito.any(VerificationToken::class.java)))
            .thenAnswer { it.arguments[0] }

        val token = service.issueToken(1, VerificationTokenType.PASSWORD_RESET, Duration.ofHours(1))

        assertNotNull(token)
        Mockito.verify(verificationTokenRepository)
            .deleteByUserIdAndTypeAndUsedAtIsNull(1, VerificationTokenType.PASSWORD_RESET)

        val captor = ArgumentCaptor.forClass(VerificationToken::class.java)
        Mockito.verify(verificationTokenRepository).save(captor.capture())
        assertEquals(1, captor.value.userId)
        assertEquals(VerificationTokenType.PASSWORD_RESET, captor.value.type)
        assertEquals(token, captor.value.token)
    }

    @Test
    fun consumeTokenMarksUsedAtOnValidToken() {
        val record =
            VerificationToken(
                id = 1,
                userId = 1,
                token = "abc",
                type = VerificationTokenType.EMAIL_CONFIRMATION,
                expiresAt = LocalDateTime.now().plusHours(1),
            )
        Mockito.`when`(
            verificationTokenRepository.findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(
                eqK("abc"),
                eqK(VerificationTokenType.EMAIL_CONFIRMATION),
                anyK(LocalDateTime::class.java),
            ),
        ).thenReturn(Optional.of(record))
        Mockito.`when`(verificationTokenRepository.save(Mockito.any(VerificationToken::class.java)))
            .thenAnswer { it.arguments[0] }

        val result = service.consumeToken("abc", VerificationTokenType.EMAIL_CONFIRMATION)

        assertNotNull(result.usedAt)
    }

    @Test
    fun consumeTokenThrowsWhenNotFoundOrExpiredOrUsed() {
        Mockito.`when`(
            verificationTokenRepository.findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(
                eqK("bad"),
                eqK(VerificationTokenType.PASSWORD_RESET),
                anyK(LocalDateTime::class.java),
            ),
        ).thenReturn(Optional.empty())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.consumeToken("bad", VerificationTokenType.PASSWORD_RESET)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }
}
