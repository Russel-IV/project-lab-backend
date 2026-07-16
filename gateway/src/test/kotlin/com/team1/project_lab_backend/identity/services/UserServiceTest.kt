package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import feign.FeignException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserServiceTest {
    private val userFeignClient = Mockito.mock(UserFeignClient::class.java)
    private val userService = UserService(userFeignClient)

    @Test
    fun createUserMapsFeignBadRequestToBadRequest() {
        Mockito.`when`(userFeignClient.create(UserUpsertRequest(name = "  ")))
            .thenThrow(FeignException.BadRequest::class.java)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                userService.createUser(UserRequest(name = "  "))
            }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createUserReturnsPersistedUser() {
        Mockito.`when`(userFeignClient.create(UserUpsertRequest(name = "Alice")))
            .thenReturn(User(id = 1, name = "Alice", email = null))

        val response = userService.createUser(UserRequest(name = "Alice"))

        assertEquals(1, response.id)
        assertEquals("Alice", response.name)
    }

    @Test
    fun updateUserReturnsNotFoundWhenMissing() {
        Mockito.`when`(userFeignClient.update(42, 42, UserUpsertRequest(name = "Updated")))
            .thenThrow(FeignException.NotFound::class.java)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                userService.updateUser(42, UserRequest(name = "Updated"), 42)
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun updateUserRejectsNonOwner() {
        Mockito.`when`(userFeignClient.update(1, 2, UserUpsertRequest(name = "Bob")))
            .thenThrow(FeignException.Forbidden::class.java)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                userService.updateUser(1, UserRequest(name = "Bob"), 2)
            }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }

    @Test
    fun updateUserReturnsUpdatedUser() {
        Mockito.`when`(userFeignClient.update(1, 1, UserUpsertRequest(name = "Bob")))
            .thenReturn(User(id = 1, name = "Bob", email = null))

        val result = userService.updateUser(1, UserRequest(name = "Bob"), 1)

        assertEquals(1, result.id)
        assertEquals("Bob", result.name)
    }

    @Test
    fun deleteUserReturnsNotFoundWhenMissing() {
        Mockito.`when`(userFeignClient.delete(99, 99)).thenThrow(FeignException.NotFound::class.java)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                userService.deleteUser(99, 99)
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun deleteUserInvokesFeignClient() {
        userService.deleteUser(1, 1)

        Mockito.verify(userFeignClient).delete(1, 1)
    }
}
