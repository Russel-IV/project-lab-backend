package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AuthFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun login(request: LoginRequest): AuthResponse =
        webClient.post().uri("/internal/auth/login").bodyValue(request).retrieve().awaitBody()

    suspend fun signup(request: SignupRequest): AuthResponse =
        webClient.post().uri("/internal/auth/signup").bodyValue(request).retrieve().awaitBody()
}

data class LoginRequest(val email: String, val password: String)

data class SignupRequest(val name: String, val email: String, val password: String)
