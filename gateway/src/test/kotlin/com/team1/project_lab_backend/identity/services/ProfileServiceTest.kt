package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.media.services.MediaFeignClient
import com.team1.project_lab_backend.media.services.MediaResponse
import com.team1.project_lab_backend.util.FieldValidationException
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets

class ProfileServiceTest {
    private val profileFeignClient = Mockito.mock(ProfileFeignClient::class.java)
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)
    private val service = ProfileService(profileFeignClient, mediaFeignClient)

    private fun profileResponse(id: Int = 1) =
        ProfileResponse(
            id = id,
            name = "Ada Lovelace",
            email = "ada@example.com",
            phone = "+1 555 123 4567",
            profilePictureUrl = null,
        )

    private fun baseRequest(
        name: String = "Ada Lovelace",
        email: String = "ada@example.com",
        phone: String? = "+1 555 123 4567",
    ) = UpdateProfileRequest(name = name, email = email, phone = phone)

    private fun feignBadRequest(body: String) =
        FeignException.BadRequest(
            "bad request",
            Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
            body.toByteArray(StandardCharsets.UTF_8),
            emptyMap(),
        )

    private fun feignUnprocessable(body: String) =
        FeignException.UnprocessableEntity(
            "unprocessable",
            Request.create(Request.HttpMethod.PATCH, "/", emptyMap(), null, RequestTemplate()),
            body.toByteArray(StandardCharsets.UTF_8),
            emptyMap(),
        )

    // ---- getProfile ----

    @Test
    fun getProfileReturnsMappedFields() {
        Mockito.`when`(profileFeignClient.getProfile(1)).thenReturn(profileResponse())

        val result = service.getProfile(1)

        assertEquals("Ada Lovelace", result.name)
        assertEquals("ada@example.com", result.email)
    }

    @Test
    fun getProfileRejectsUnknownUser() {
        Mockito.`when`(profileFeignClient.getProfile(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { service.getProfile(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- updateProfile ----

    @Test
    fun updateProfileReturnsUpdatedFields() {
        val request = baseRequest(name = "Ada L.", email = "new@example.com", phone = null)
        Mockito.`when`(
            profileFeignClient.updateProfile(1, ProfileUpdateRequest(name = "Ada L.", email = "new@example.com", phone = null)),
        ).thenReturn(profileResponse().copy(name = "Ada L.", email = "new@example.com", phone = null))

        val result = service.updateProfile(1, request)

        assertEquals("Ada L.", result.name)
    }

    @Test
    fun updateProfileMapsFeignFieldErrors() {
        val request = baseRequest(email = "taken@example.com")
        Mockito.`when`(
            profileFeignClient.updateProfile(
                1,
                ProfileUpdateRequest(name = "Ada Lovelace", email = "taken@example.com", phone = "+1 555 123 4567"),
            ),
        ).thenThrow(feignUnprocessable("""{"errors":{"email":"email already in use"}}"""))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.updateProfile(1, request)
            }
        assertEquals("email already in use", ex.errors["email"])
    }

    // ---- uploadProfilePicture ----

    @Test
    fun uploadProfilePictureSavesFileAndUpdatesUser() {
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0)).thenReturn(
            MediaResponse(
                id = 7,
                ownerType = "USER",
                ownerId = 1,
                url = "http://localhost:8080/uploads/users/1/new-key.png",
                caption = null,
                isPrimary = false,
                displayOrder = 0,
            ),
        )
        Mockito.`when`(profileFeignClient.updatePictureUrl(1, UpdatePictureUrlRequest("http://localhost:8080/uploads/users/1/new-key.png")))
            .thenReturn(profileResponse().copy(profilePictureUrl = "http://localhost:8080/uploads/users/1/new-key.png"))

        val result = service.uploadProfilePicture(1, file)

        assertEquals("http://localhost:8080/uploads/users/1/new-key.png", result.profilePictureUrl)
    }

    @Test
    fun uploadProfilePictureDeletesPreviousPicture() {
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(
            listOf(
                MediaResponse(
                    id = 3,
                    ownerType = "USER",
                    ownerId = 1,
                    url = "http://localhost:8080/uploads/users/1/old-key.png",
                    caption = null,
                    isPrimary = false,
                    displayOrder = 0,
                ),
            ),
        )
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0)).thenReturn(
            MediaResponse(
                id = 7,
                ownerType = "USER",
                ownerId = 1,
                url = "http://localhost:8080/uploads/users/1/new-key.png",
                caption = null,
                isPrimary = false,
                displayOrder = 0,
            ),
        )
        Mockito.`when`(profileFeignClient.updatePictureUrl(1, UpdatePictureUrlRequest("http://localhost:8080/uploads/users/1/new-key.png")))
            .thenReturn(profileResponse().copy(profilePictureUrl = "http://localhost:8080/uploads/users/1/new-key.png"))

        service.uploadProfilePicture(1, file)

        Mockito.verify(mediaFeignClient).delete("USER", 1, 3)
    }

    @Test
    fun uploadProfilePictureMapsFeignBadRequestToResponseStatusException() {
        val file = MockMultipartFile("file", "notes.txt", "text/plain", byteArrayOf(1))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0))
            .thenThrow(feignBadRequest("""{"message":"file must be an image (got: text/plain)"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { service.uploadProfilePicture(1, file) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("file must be an image (got: text/plain)", ex.reason)
    }

    // ---- changePassword ----

    @Test
    fun changePasswordRejectsWrongCurrentPassword() {
        Mockito.`when`(profileFeignClient.changePassword(1, PasswordChangeRequest("wrong", "new-password")))
            .thenThrow(feignUnprocessable("""{"errors":{"currentPassword":"current password is incorrect"}}"""))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.changePassword(1, ChangePasswordRequest(currentPassword = "wrong", newPassword = "new-password"))
            }
        assertEquals("current password is incorrect", ex.errors["currentPassword"])
    }

    @Test
    fun changePasswordSucceeds() {
        service.changePassword(1, ChangePasswordRequest(currentPassword = "old-pass", newPassword = "new-password"))

        Mockito.verify(profileFeignClient).changePassword(1, PasswordChangeRequest("old-pass", "new-password"))
    }

    // ---- deleteAccount ----

    @Test
    fun deleteAccountInvokesFeignClient() {
        service.deleteAccount(1)

        Mockito.verify(profileFeignClient).deleteAccount(1)
    }

    @Test
    fun deleteAccountRejectsUnknownUser() {
        Mockito.`when`(profileFeignClient.deleteAccount(99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) { service.deleteAccount(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }
}
