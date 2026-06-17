package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.resolvers.AuthPayload
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
) {
    fun signup(name: String, email: String, rawPassword: String): AuthPayload {
        if (userRepository.findByEmail(email).isPresent)
            throw ResponseStatusException(HttpStatus.CONFLICT, "email already in use")
        val user = userRepository.save(
            User(name = name, email = email, passwordHash = passwordEncoder.encode(rawPassword)),
        )
        return AuthPayload(token = jwtService.generateToken(user), user = user)
    }

    fun login(email: String, rawPassword: String): AuthPayload {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials") }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash))
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials")
        return AuthPayload(token = jwtService.generateToken(user), user = user)
    }
}
