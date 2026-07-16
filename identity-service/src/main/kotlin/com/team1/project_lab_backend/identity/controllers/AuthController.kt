package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.AuthResponse
import com.team1.project_lab_backend.identity.dto.LoginRequest
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
}
