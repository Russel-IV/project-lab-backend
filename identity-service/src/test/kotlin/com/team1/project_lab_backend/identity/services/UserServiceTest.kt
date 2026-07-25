package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID

class UserServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val service = UserService(userRepository)

    @Test
    fun createUserRejectsBlankName() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.createUser(UserRequest(name = "  "))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createUserSavesUser() {
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }
        val result = service.createUser(UserRequest(name = "Ada"))
        assertEquals("Ada", result.name)
    }

    @Test
    fun getUserByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(userRepository.findById(99)).thenReturn(Optional.empty())
        val ex = assertThrows(ResponseStatusException::class.java) { service.getUserById(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateUserRejectsNonOwner() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updateUser(1, UserRequest(name = "New"), requestingUserId = 2)
            }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun updateUserRejectsUnknownUser() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.empty())
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updateUser(1, UserRequest(name = "New"), requestingUserId = 1)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateUserPreservesPublicId() {
        val existing = User(id = 1, name = "Old", email = "ada@example.com")
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existing))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateUser(1, UserRequest(name = "New"), requestingUserId = 1)

        assertEquals(existing.publicId, result.publicId)
    }

    @Test
    fun getUserByPublicIdReturnsNotFoundWhenMissing() {
        val publicId = UUID.randomUUID()
        Mockito.`when`(userRepository.findByPublicId(publicId)).thenReturn(Optional.empty())
        val ex = assertThrows(ResponseStatusException::class.java) { service.getUserByPublicId(publicId) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getUserByPublicIdDelegatesToRepository() {
        val user = User(id = 1, name = "Ada")
        Mockito.`when`(userRepository.findByPublicId(user.publicId)).thenReturn(Optional.of(user))
        val result = service.getUserByPublicId(user.publicId)
        assertEquals(1, result.id)
    }

    @Test
    fun deleteUserRejectsNonOwner() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.deleteUser(1, requestingUserId = 2)
            }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun getUsersByIdsDelegatesToRepository() {
        Mockito.`when`(userRepository.findAllById(listOf(1, 2))).thenReturn(listOf(User(id = 1, name = "A"), User(id = 2, name = "B")))
        val result = service.getUsersByIds(listOf(1, 2))
        assertEquals(2, result.size)
    }
}
