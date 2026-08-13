package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.models.VerificationToken
import com.team1.project_lab_backend.identity.models.VerificationTokenType
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

class AuthServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val jwtService = Mockito.mock(JwtService::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private val emailService = Mockito.mock(EmailService::class.java)
    private val verificationTokenService = Mockito.mock(VerificationTokenService::class.java)
    private val service =
        AuthService(
            userRepository,
            jwtService,
            passwordEncoder,
            emailService,
            verificationTokenService,
            "http://localhost:5173",
        )

    @Test
    fun signupRejectsExistingEmail() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com"))
            .thenReturn(Optional.of(User(id = 1, name = "Ada", email = "ada@example.com")))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.signup("Ada", "ada@example.com", "password123")
            }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun signupCreatesUserAndReturnsToken() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.empty())
        Mockito.`when`(passwordEncoder.encode("password123")).thenReturn("hashed")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java)))
            .thenAnswer { (it.arguments[0] as User) }
        Mockito.`when`(jwtService.generateToken(Mockito.any(User::class.java) ?: User(name = "")))
            .thenReturn("jwt-token")
        Mockito.`when`(
            verificationTokenService.issueToken(
                Mockito.anyInt(),
                Mockito.eq(VerificationTokenType.EMAIL_CONFIRMATION) ?: VerificationTokenType.EMAIL_CONFIRMATION,
                Mockito.any(Duration::class.java) ?: Duration.ZERO,
            ),
        ).thenReturn("confirm-token")

        val result = service.signup("Ada", "ada@example.com", "password123")

        assertEquals("jwt-token", result.token)
        assertEquals("ada@example.com", result.user.email)
        Mockito.verify(emailService).sendWelcomeEmail("ada@example.com", "Ada")
        Mockito.verify(emailService)
            .sendAccountConfirmationEmail("ada@example.com", "Ada", "http://localhost:5173/confirm-account?token=confirm-token")
        Mockito.verify(verificationTokenService)
            .issueToken(0, VerificationTokenType.EMAIL_CONFIRMATION, Duration.ofHours(24))
    }

    @Test
    fun loginRejectsUnknownEmail() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("nobody@example.com")).thenReturn(Optional.empty())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.login("nobody@example.com", "password123")
            }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun loginRejectsWrongPassword() {
        val user = User(id = 1, name = "Ada", email = "ada@example.com", passwordHash = "hashed")
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(user))
        Mockito.`when`(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.login("ada@example.com", "wrong")
            }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun loginReturnsTokenOnSuccess() {
        val user = User(id = 1, name = "Ada", email = "ada@example.com", passwordHash = "hashed")
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(user))
        Mockito.`when`(passwordEncoder.matches("correct", "hashed")).thenReturn(true)
        Mockito.`when`(jwtService.generateToken(user)).thenReturn("jwt-token")

        val result = service.login("ada@example.com", "correct")

        assertEquals("jwt-token", result.token)
    }

    @Test
    fun requestPasswordResetSendsEmailForExistingUser() {
        val user = User(id = 1, name = "Ada", email = "ada@example.com")
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(user))
        Mockito.`when`(
            verificationTokenService.issueToken(1, VerificationTokenType.PASSWORD_RESET, Duration.ofHours(1)),
        ).thenReturn("reset-token")

        service.requestPasswordReset("ada@example.com")

        Mockito.verify(emailService)
            .sendPasswordResetEmail("ada@example.com", "Ada", "http://localhost:5173/reset-password?token=reset-token")
    }

    @Test
    fun requestPasswordResetDoesNothingForUnknownEmail() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("nobody@example.com")).thenReturn(Optional.empty())

        service.requestPasswordReset("nobody@example.com")

        Mockito.verifyNoInteractions(emailService)
    }

    @Test
    fun resetPasswordRejectsShortPassword() {
        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.resetPassword("some-token", "short")
            }
        assertEquals(true, ex.errors.containsKey("newPassword"))
        Mockito.verifyNoInteractions(verificationTokenService)
    }

    @Test
    fun resetPasswordRejectsInvalidToken() {
        Mockito.`when`(verificationTokenService.consumeToken("bad-token", VerificationTokenType.PASSWORD_RESET))
            .thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid or expired token"))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.resetPassword("bad-token", "newpassword123")
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun resetPasswordUpdatesPasswordOnValidToken() {
        val record =
            VerificationToken(
                id = 1,
                userId = 1,
                token = "good-token",
                type = VerificationTokenType.PASSWORD_RESET,
                expiresAt = LocalDateTime.now().plusHours(1),
            )
        val existing = User(id = 1, name = "Ada", email = "ada@example.com", passwordHash = "old-hash")
        Mockito.`when`(verificationTokenService.consumeToken("good-token", VerificationTokenType.PASSWORD_RESET))
            .thenReturn(record)
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existing))
        Mockito.`when`(passwordEncoder.encode("newpassword123")).thenReturn("new-hash")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java)))
            .thenAnswer { it.arguments[0] as User }

        service.resetPassword("good-token", "newpassword123")

        Mockito.verify(userRepository).save(Mockito.argThat { it.passwordHash == "new-hash" })
    }

    @Test
    fun confirmAccountRejectsInvalidToken() {
        Mockito.`when`(verificationTokenService.consumeToken("bad-token", VerificationTokenType.EMAIL_CONFIRMATION))
            .thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid or expired token"))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.confirmAccount("bad-token")
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun confirmAccountSetsTimestampOnValidToken() {
        val record =
            VerificationToken(
                id = 1,
                userId = 1,
                token = "good-token",
                type = VerificationTokenType.EMAIL_CONFIRMATION,
                expiresAt = LocalDateTime.now().plusHours(1),
            )
        val existing = User(id = 1, name = "Ada", email = "ada@example.com")
        Mockito.`when`(verificationTokenService.consumeToken("good-token", VerificationTokenType.EMAIL_CONFIRMATION))
            .thenReturn(record)
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existing))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java)))
            .thenAnswer { it.arguments[0] as User }

        service.confirmAccount("good-token")

        Mockito.verify(userRepository).save(
            Mockito.argThat {
                assertNotNull(it.emailConfirmedAt)
                true
            },
        )
    }
}
