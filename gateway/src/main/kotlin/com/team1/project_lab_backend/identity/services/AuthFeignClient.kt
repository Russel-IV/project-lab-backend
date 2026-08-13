package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Component
class AuthFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun login(request: LoginRequest): AuthResponse =
        webClient.post().uri("/internal/auth/login").bodyValue(request).retrieve().awaitBody()

    suspend fun signup(request: SignupRequest): AuthResponse =
        webClient.post().uri("/internal/auth/signup").bodyValue(request).retrieve().awaitBody()

    suspend fun requestPasswordReset(request: PasswordResetRequestRequest) {
        webClient.post().uri("/internal/auth/password-reset/request").bodyValue(request).retrieve().awaitBodilessEntity()
    }

    suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest) {
        webClient.post().uri("/internal/auth/password-reset/confirm").bodyValue(request).retrieve().awaitBodilessEntity()
    }

    suspend fun confirmAccount(request: ConfirmAccountRequest) {
        webClient.post().uri("/internal/auth/confirm-account").bodyValue(request).retrieve().awaitBodilessEntity()
    }
}

data class LoginRequest(val email: String, val password: String)

data class SignupRequest(val name: String, val email: String, val password: String)

data class PasswordResetRequestRequest(val email: String)

data class PasswordResetConfirmRequest(val token: String, val newPassword: String)

data class ConfirmAccountRequest(val token: String)
