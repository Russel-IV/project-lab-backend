package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.media.services.MediaFeignClient
import com.team1.project_lab_backend.media.services.MediaResponse
import com.team1.project_lab_backend.util.FakeFilePart
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

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

    // ---- getProfile ----

    @Test
    fun getProfileReturnsMappedFields() =
        runTest {
            Mockito.`when`(profileFeignClient.getProfile(1)).thenReturn(profileResponse())

            val result = service.getProfile(1)

            assertEquals("Ada Lovelace", result.name)
            assertEquals("ada@example.com", result.email)
        }

    @Test
    fun getProfileRejectsUnknownUser() =
        runTest {
            Mockito.`when`(profileFeignClient.getProfile(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.getProfile(99) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- updateProfile ----

    @Test
    fun updateProfileReturnsUpdatedFields() =
        runTest {
            val request = baseRequest(name = "Ada L.", email = "new@example.com", phone = null)
            Mockito.`when`(
                profileFeignClient.updateProfile(1, ProfileUpdateRequest(name = "Ada L.", email = "new@example.com", phone = null)),
            ).thenReturn(profileResponse().copy(name = "Ada L.", email = "new@example.com", phone = null))

            val result = service.updateProfile(1, request)

            assertEquals("Ada L.", result.name)
        }

    @Test
    fun updateProfileMapsFeignFieldErrors() =
        runTest {
            val request = baseRequest(email = "taken@example.com")
            Mockito.`when`(
                profileFeignClient.updateProfile(
                    1,
                    ProfileUpdateRequest(name = "Ada Lovelace", email = "taken@example.com", phone = "+1 555 123 4567"),
                ),
            ).thenThrow(webClientException(422, """{"errors":{"email":"email already in use"}}"""))

            val ex = assertThrowsSuspend<FieldValidationException> { service.updateProfile(1, request) }
            assertEquals("email already in use", ex.errors["email"])
        }

    // ---- uploadProfilePicture ----

    @Test
    fun uploadProfilePictureSavesFileAndUpdatesUser() =
        runTest {
            val file = FakeFilePart("file", "avatar.png", byteArrayOf(1, 2, 3))
            Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
            Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0)).thenReturn(
                MediaResponse(
                    id = 7,
                    ownerType = "USER",
                    ownerId = 1,
                    url = "http://localhost:8080/uploads/users/1/new-key.png",
                    thumbnailUrl = "http://localhost:8080/uploads/users/1/new-key.png",
                    url1024 = null,
                    url512 = null,
                    caption = null,
                    isPrimary = false,
                    displayOrder = 0,
                ),
            )
            Mockito.`when`(
                profileFeignClient.updatePictureUrl(1, UpdatePictureUrlRequest("http://localhost:8080/uploads/users/1/new-key.png")),
            ).thenReturn(profileResponse().copy(profilePictureUrl = "http://localhost:8080/uploads/users/1/new-key.png"))

            val result = service.uploadProfilePicture(1, file)

            assertEquals("http://localhost:8080/uploads/users/1/new-key.png", result.profilePictureUrl)
        }

    @Test
    fun uploadProfilePictureDeletesPreviousPicture() =
        runTest {
            val file = FakeFilePart("file", "avatar.png", byteArrayOf(1, 2, 3))
            Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(
                listOf(
                    MediaResponse(
                        id = 3,
                        ownerType = "USER",
                        ownerId = 1,
                        url = "http://localhost:8080/uploads/users/1/old-key.png",
                        thumbnailUrl = "http://localhost:8080/uploads/users/1/old-key.png",
                        url1024 = null,
                        url512 = null,
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
                    thumbnailUrl = "http://localhost:8080/uploads/users/1/new-key.png",
                    url1024 = null,
                    url512 = null,
                    caption = null,
                    isPrimary = false,
                    displayOrder = 0,
                ),
            )
            Mockito.`when`(
                profileFeignClient.updatePictureUrl(1, UpdatePictureUrlRequest("http://localhost:8080/uploads/users/1/new-key.png")),
            ).thenReturn(profileResponse().copy(profilePictureUrl = "http://localhost:8080/uploads/users/1/new-key.png"))

            service.uploadProfilePicture(1, file)

            Mockito.verify(mediaFeignClient).delete("USER", 1, 3)
        }

    @Test
    fun uploadProfilePictureMapsFeignBadRequestToResponseStatusException() =
        runTest {
            val file = FakeFilePart("file", "notes.txt", byteArrayOf(1))
            Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
            Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0))
                .thenThrow(webClientException(400, """{"message":"file must be an image (got: text/plain)"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.uploadProfilePicture(1, file) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("file must be an image (got: text/plain)", ex.reason)
        }

    // ---- changePassword ----

    @Test
    fun changePasswordRejectsWrongCurrentPassword() =
        runTest {
            Mockito.`when`(profileFeignClient.changePassword(1, PasswordChangeRequest("wrong", "new-password")))
                .thenThrow(webClientException(422, """{"errors":{"currentPassword":"current password is incorrect"}}"""))

            val ex =
                assertThrowsSuspend<FieldValidationException> {
                    service.changePassword(1, ChangePasswordRequest(currentPassword = "wrong", newPassword = "new-password"))
                }
            assertEquals("current password is incorrect", ex.errors["currentPassword"])
        }

    @Test
    fun changePasswordSucceeds() =
        runTest {
            service.changePassword(1, ChangePasswordRequest(currentPassword = "old-pass", newPassword = "new-password"))

            Mockito.verify(profileFeignClient).changePassword(1, PasswordChangeRequest("old-pass", "new-password"))
        }

    // ---- deleteAccount ----

    @Test
    fun deleteAccountInvokesFeignClient() =
        runTest {
            service.deleteAccount(1)

            Mockito.verify(profileFeignClient).deleteAccount(1)
        }

    @Test
    fun deleteAccountRejectsUnknownUser() =
        runTest {
            Mockito.`when`(profileFeignClient.deleteAccount(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.deleteAccount(99) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }
}
