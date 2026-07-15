package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): password hashing, JWT issuance, and email
 * uniqueness all now live in identity-service, reached via authFeignClient.
 */
@Service
class AuthService(private val authFeignClient: AuthFeignClient) {

    fun signup(name: String, email: String, rawPassword: String): AuthResponse =
        try {
            authFeignClient.signup(SignupRequest(name = name, email = email, password = rawPassword))
        } catch (e: FeignException.Conflict) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "email already in use")
        }

    fun login(email: String, rawPassword: String): AuthResponse =
        try {
            authFeignClient.login(LoginRequest(email = email, password = rawPassword))
        } catch (e: FeignException.Unauthorized) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials")
        }
}
