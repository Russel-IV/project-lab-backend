package com.team1.project_lab_backend.identity.dto

data class LoginRequest(
    val email: String,
    val password: String,
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class AuthResponse(
    val token: String,
    val user: ProfileResponse,
)
