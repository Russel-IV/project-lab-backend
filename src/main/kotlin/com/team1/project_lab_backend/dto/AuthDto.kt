package com.team1.project_lab_backend.dto

import com.team1.project_lab_backend.models.User

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
    val user: User,
)
