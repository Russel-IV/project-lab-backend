package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import com.team1.project_lab_backend.identity.dto.toProfileResponse
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.models.VerificationTokenType
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val verificationTokenService: VerificationTokenService,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {
    fun signup(
        name: String,
        email: String,
        rawPassword: String,
    ): AuthResponse {
        if (userRepository.findByEmailAndDeletedAtIsNull(email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "email already in use")
        }
        val user =
            userRepository.save(
                User(name = name, email = email, passwordHash = passwordEncoder.encode(rawPassword)),
            )
        emailService.sendWelcomeEmail(email, user.name)
        val confirmToken =
            verificationTokenService.issueToken(user.id, VerificationTokenType.EMAIL_CONFIRMATION, Duration.ofHours(24))
        emailService.sendAccountConfirmationEmail(email, user.name, "$frontendUrl/confirm-account?token=$confirmToken")
        return AuthResponse(token = jwtService.generateToken(user), user = user.toProfileResponse())
    }

    fun login(
        email: String,
        rawPassword: String,
    ): AuthResponse {
        val user =
            userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials") }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials")
        }
        return AuthResponse(token = jwtService.generateToken(user), user = user.toProfileResponse())
    }

    // Always returns normally, whether or not the email exists — the caller must never be
    // able to distinguish the two cases (no user enumeration via this endpoint).
    fun requestPasswordReset(email: String) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent { user ->
            val token = verificationTokenService.issueToken(user.id, VerificationTokenType.PASSWORD_RESET, Duration.ofHours(1))
            emailService.sendPasswordResetEmail(email, user.name, "$frontendUrl/reset-password?token=$token")
        }
    }

    fun resetPassword(
        token: String,
        newPassword: String,
    ) {
        if (newPassword.length < 8) {
            throw FieldValidationException(mapOf("newPassword" to "new password must be at least 8 characters"))
        }
        val record = verificationTokenService.consumeToken(token, VerificationTokenType.PASSWORD_RESET)
        val existing =
            userRepository.findById(record.userId)
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid or expired token") }
        userRepository.save(
            User(
                id = existing.id,
                publicId = existing.publicId,
                name = existing.name,
                email = existing.email,
                passwordHash = passwordEncoder.encode(newPassword),
                phone = existing.phone,
                profilePictureUrl = existing.profilePictureUrl,
                deletedAt = existing.deletedAt,
                emailConfirmedAt = existing.emailConfirmedAt,
            ),
        )
    }

    fun confirmAccount(token: String) {
        val record = verificationTokenService.consumeToken(token, VerificationTokenType.EMAIL_CONFIRMATION)
        val existing =
            userRepository.findById(record.userId)
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid or expired token") }
        userRepository.save(
            User(
                id = existing.id,
                publicId = existing.publicId,
                name = existing.name,
                email = existing.email,
                passwordHash = existing.passwordHash,
                phone = existing.phone,
                profilePictureUrl = existing.profilePictureUrl,
                deletedAt = existing.deletedAt,
                emailConfirmedAt = LocalDateTime.now(),
            ),
        )
    }
}
