package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class UserServiceTest {
    private val userFeignClient = Mockito.mock(UserFeignClient::class.java)
    private val userService = UserService(userFeignClient)

    @Test
    fun createUserMapsFeignBadRequestToBadRequest() =
        runTest {
            Mockito.`when`(userFeignClient.create(UserUpsertRequest(name = "  ")))
                .thenThrow(webClientException(400))

            val exception = assertThrowsSuspend<ResponseStatusException> { userService.createUser(UserRequest(name = "  ")) }
            assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        }

    @Test
    fun createUserReturnsPersistedUser() =
        runTest {
            Mockito.`when`(userFeignClient.create(UserUpsertRequest(name = "Alice")))
                .thenReturn(User(id = 1, publicId = UUID.randomUUID(), name = "Alice", email = null))

            val response = userService.createUser(UserRequest(name = "Alice"))

            assertEquals(1, response.id)
            assertEquals("Alice", response.name)
        }

    @Test
    fun getUserByPublicIdReturnsNotFoundWhenMissing() =
        runTest {
            val publicId = UUID.randomUUID()
            Mockito.`when`(userFeignClient.getByPublicId(publicId)).thenThrow(webClientException(404))

            val exception = assertThrowsSuspend<ResponseStatusException> { userService.getUserByPublicId(publicId) }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

    @Test
    fun getUserByPublicIdDelegatesToFeignClient() =
        runTest {
            val publicId = UUID.randomUUID()
            Mockito.`when`(userFeignClient.getByPublicId(publicId))
                .thenReturn(User(id = 1, publicId = publicId, name = "Alice", email = null))

            val result = userService.getUserByPublicId(publicId)

            assertEquals(publicId, result.publicId)
        }

    @Test
    fun updateUserReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(userFeignClient.update(42, 42, UserUpsertRequest(name = "Updated")))
                .thenThrow(webClientException(404))

            val exception =
                assertThrowsSuspend<ResponseStatusException> { userService.updateUser(42, UserRequest(name = "Updated"), 42) }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

    @Test
    fun updateUserRejectsNonOwner() =
        runTest {
            Mockito.`when`(userFeignClient.update(1, 2, UserUpsertRequest(name = "Bob")))
                .thenThrow(webClientException(403))

            val exception = assertThrowsSuspend<ResponseStatusException> { userService.updateUser(1, UserRequest(name = "Bob"), 2) }
            assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        }

    @Test
    fun updateUserReturnsUpdatedUser() =
        runTest {
            Mockito.`when`(userFeignClient.update(1, 1, UserUpsertRequest(name = "Bob")))
                .thenReturn(User(id = 1, publicId = UUID.randomUUID(), name = "Bob", email = null))

            val result = userService.updateUser(1, UserRequest(name = "Bob"), 1)

            assertEquals(1, result.id)
            assertEquals("Bob", result.name)
        }

    @Test
    fun deleteUserReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(userFeignClient.delete(99, 99)).thenThrow(webClientException(404))

            val exception = assertThrowsSuspend<ResponseStatusException> { userService.deleteUser(99, 99) }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

    @Test
    fun deleteUserInvokesFeignClient() =
        runTest {
            userService.deleteUser(1, 1)

            Mockito.verify(userFeignClient).delete(1, 1)
        }
}
