package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.webClientErrorMessage
import com.team1.project_lab_backend.util.webClientFieldErrors
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): password hashing, JWT issuance, and email
 * uniqueness all now live in identity-service, reached via authFeignClient.
 */
@Service
class AuthService(private val authFeignClient: AuthFeignClient) {
    suspend fun signup(
        name: String,
        email: String,
        rawPassword: String,
    ): AuthResponse =
        try {
            authFeignClient.signup(SignupRequest(name = name, email = email, password = rawPassword))
        } catch (e: WebClientResponseException.Conflict) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "email already in use")
        }

    suspend fun login(
        email: String,
        rawPassword: String,
    ): AuthResponse =
        try {
            authFeignClient.login(LoginRequest(email = email, password = rawPassword))
        } catch (e: WebClientResponseException.Unauthorized) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials")
        }

    suspend fun requestPasswordReset(email: String) {
        authFeignClient.requestPasswordReset(PasswordResetRequestRequest(email = email))
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String,
    ) {
        try {
            authFeignClient.confirmPasswordReset(PasswordResetConfirmRequest(token = token, newPassword = newPassword))
        } catch (e: WebClientResponseException.UnprocessableContent) {
            throw FieldValidationException(webClientFieldErrors(e) ?: emptyMap())
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid or expired token")
        }
    }

    suspend fun confirmAccount(token: String) {
        try {
            authFeignClient.confirmAccount(ConfirmAccountRequest(token = token))
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid or expired token")
        }
    }
}
