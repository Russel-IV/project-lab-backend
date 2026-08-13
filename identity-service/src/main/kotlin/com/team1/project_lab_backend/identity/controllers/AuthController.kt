package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.AuthResponse
import com.team1.project_lab_backend.identity.dto.ConfirmAccountRequest
import com.team1.project_lab_backend.identity.dto.LoginRequest
import com.team1.project_lab_backend.identity.dto.PasswordResetConfirmRequest
import com.team1.project_lab_backend.identity.dto.PasswordResetRequestRequest
import com.team1.project_lab_backend.identity.dto.SignupRequest
import com.team1.project_lab_backend.identity.services.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> = ResponseEntity.ok(authService.login(request.email, request.password))

    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest,
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.signup(request.name, request.email, request.password))

    @PostMapping("/password-reset/request")
    fun requestPasswordReset(
        @RequestBody request: PasswordResetRequestRequest,
    ): ResponseEntity<Void> {
        authService.requestPasswordReset(request.email)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/password-reset/confirm")
    fun confirmPasswordReset(
        @RequestBody request: PasswordResetConfirmRequest,
    ): ResponseEntity<Void> {
        authService.resetPassword(request.token, request.newPassword)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/confirm-account")
    fun confirmAccount(
        @RequestBody request: ConfirmAccountRequest,
    ): ResponseEntity<Void> {
        authService.confirmAccount(request.token)
        return ResponseEntity.noContent().build()
    }
}
