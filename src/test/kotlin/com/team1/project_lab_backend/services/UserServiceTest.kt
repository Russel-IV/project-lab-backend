package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.UserRequest
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val userService = UserService(userRepository)

    @Test
    fun createUserRejectsBlankName() {
        val exception = assertThrows(ResponseStatusException::class.java) {
            userService.createUser(UserRequest(name = "  "))
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createUserReturnsPersistedUser() {
        val saved = User(id = 1, name = "Alice")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenReturn(saved)

        val response = userService.createUser(UserRequest(name = "Alice"))

        assertEquals(1, response.id)
        assertEquals("Alice", response.name)
    }

    @Test
    fun updateUserReturnsNotFoundWhenMissing() {
        Mockito.`when`(userRepository.existsById(42)).thenReturn(false)

        val exception = assertThrows(ResponseStatusException::class.java) {
            userService.updateUser(42, UserRequest(name = "Updated"))
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }
}
