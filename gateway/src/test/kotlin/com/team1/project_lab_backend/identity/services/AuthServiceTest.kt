package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.AuthResponse
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class AuthServiceTest {
    private val authFeignClient = Mockito.mock(AuthFeignClient::class.java)
    private val service = AuthService(authFeignClient)

    private fun profileResponse(id: Int = 1) =
        ProfileResponse(
            id = id,
            name = "Ada Lovelace",
            email = "ada@example.com",
            phone = null,
            profilePictureUrl = null,
        )

    // ---- signup / login (pre-existing behavior, unchanged) ----

    @Test
    fun signupSucceeds() =
        runTest {
            Mockito.`when`(authFeignClient.signup(SignupRequest("Ada", "ada@example.com", "password123")))
                .thenReturn(AuthResponse(token = "jwt-token", user = profileResponse()))

            val result = service.signup("Ada", "ada@example.com", "password123")

            assertEquals("jwt-token", result.token)
        }

    @Test
    fun signupRejectsExistingEmail() =
        runTest {
            Mockito.`when`(authFeignClient.signup(SignupRequest("Ada", "ada@example.com", "password123")))
                .thenThrow(webClientException(409))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.signup("Ada", "ada@example.com", "password123") }
            assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        }

    @Test
    fun loginRejectsInvalidCredentials() =
        runTest {
            Mockito.`when`(authFeignClient.login(LoginRequest("ada@example.com", "wrong")))
                .thenThrow(webClientException(401))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.login("ada@example.com", "wrong") }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    // ---- requestPasswordReset ----

    @Test
    fun requestPasswordResetInvokesFeignClient() =
        runTest {
            service.requestPasswordReset("ada@example.com")

            Mockito.verify(authFeignClient).requestPasswordReset(PasswordResetRequestRequest("ada@example.com"))
        }

    // ---- resetPassword ----

    @Test
    fun resetPasswordSucceeds() =
        runTest {
            service.resetPassword("good-token", "newpassword123")

            Mockito.verify(authFeignClient).confirmPasswordReset(PasswordResetConfirmRequest("good-token", "newpassword123"))
        }

    @Test
    fun resetPasswordMapsFeignBadRequestToResponseStatusException() =
        runTest {
            Mockito.`when`(authFeignClient.confirmPasswordReset(PasswordResetConfirmRequest("bad-token", "newpassword123")))
                .thenThrow(webClientException(400))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.resetPassword("bad-token", "newpassword123") }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("invalid or expired token", ex.reason)
        }

    @Test
    fun resetPasswordMapsFeignFieldErrors() =
        runTest {
            Mockito.`when`(authFeignClient.confirmPasswordReset(PasswordResetConfirmRequest("good-token", "short")))
                .thenThrow(webClientException(422, """{"errors":{"newPassword":"new password must be at least 8 characters"}}"""))

            val ex = assertThrowsSuspend<FieldValidationException> { service.resetPassword("good-token", "short") }
            assertEquals("new password must be at least 8 characters", ex.errors["newPassword"])
        }

    // ---- confirmAccount ----

    @Test
    fun confirmAccountSucceeds() =
        runTest {
            service.confirmAccount("good-token")

            Mockito.verify(authFeignClient).confirmAccount(ConfirmAccountRequest("good-token"))
        }

    @Test
    fun confirmAccountMapsFeignBadRequestToResponseStatusException() =
        runTest {
            Mockito.`when`(authFeignClient.confirmAccount(ConfirmAccountRequest("bad-token")))
                .thenThrow(webClientException(400))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.confirmAccount("bad-token") }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("invalid or expired token", ex.reason)
        }
}
