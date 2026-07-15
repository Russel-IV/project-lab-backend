package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class AuthServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val jwtService = Mockito.mock(JwtService::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private val service = AuthService(userRepository, jwtService, passwordEncoder)

    @Test
    fun signupRejectsExistingEmail() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com"))
            .thenReturn(Optional.of(User(id = 1, name = "Ada", email = "ada@example.com")))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.signup("Ada", "ada@example.com", "password123")
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun signupCreatesUserAndReturnsToken() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.empty())
        Mockito.`when`(passwordEncoder.encode("password123")).thenReturn("hashed")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java)))
            .thenAnswer { (it.arguments[0] as User) }
        Mockito.`when`(jwtService.generateToken(Mockito.any(User::class.java) ?: User(name = "")))
            .thenReturn("jwt-token")

        val result = service.signup("Ada", "ada@example.com", "password123")

        assertEquals("jwt-token", result.token)
        assertEquals("ada@example.com", result.user.email)
    }

    @Test
    fun loginRejectsUnknownEmail() {
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("nobody@example.com")).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.login("nobody@example.com", "password123")
        }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun loginRejectsWrongPassword() {
        val user = User(id = 1, name = "Ada", email = "ada@example.com", passwordHash = "hashed")
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(user))
        Mockito.`when`(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.login("ada@example.com", "wrong")
        }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun loginReturnsTokenOnSuccess() {
        val user = User(id = 1, name = "Ada", email = "ada@example.com", passwordHash = "hashed")
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(user))
        Mockito.`when`(passwordEncoder.matches("correct", "hashed")).thenReturn(true)
        Mockito.`when`(jwtService.generateToken(user)).thenReturn("jwt-token")

        val result = service.login("ada@example.com", "correct")

        assertEquals("jwt-token", result.token)
    }
}
