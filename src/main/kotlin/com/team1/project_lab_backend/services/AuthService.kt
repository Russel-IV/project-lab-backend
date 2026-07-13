package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AuthResponse
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val storageService: StorageService,
) {
    fun signup(name: String, email: String, rawPassword: String): AuthResponse {
        if (userRepository.findByEmailAndDeletedAtIsNull(email).isPresent)
            throw ResponseStatusException(HttpStatus.CONFLICT, "email already in use")
        val user = userRepository.save(
            User(name = name, email = email, passwordHash = passwordEncoder.encode(rawPassword)),
        )
        return AuthResponse(token = jwtService.generateToken(user), user = user.toProfileResponse(storageService))
    }

    fun login(email: String, rawPassword: String): AuthResponse {
        val user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials") }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash))
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials")
        return AuthResponse(token = jwtService.generateToken(user), user = user.toProfileResponse(storageService))
    }
}
