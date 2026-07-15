package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "identity-service", contextId = "authFeignClient")
interface AuthFeignClient {

    @PostMapping("/internal/auth/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse

    @PostMapping("/internal/auth/signup")
    fun signup(@RequestBody request: SignupRequest): AuthResponse
}

data class LoginRequest(val email: String, val password: String)
data class SignupRequest(val name: String, val email: String, val password: String)
