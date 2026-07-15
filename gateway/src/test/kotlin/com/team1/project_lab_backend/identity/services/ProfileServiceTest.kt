package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.media.services.MediaFeignClient
import com.team1.project_lab_backend.media.services.MediaResponse
import com.team1.project_lab_backend.util.FieldValidationException
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.util.Optional

class ProfileServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private val service = ProfileService(userRepository, mediaFeignClient, passwordEncoder)

    private fun existingUser(
        id: Int = 1,
        email: String? = "ada@example.com",
        phone: String? = "+1 555 123 4567",
    ) = User(id = id, name = "Ada Lovelace", email = email, passwordHash = "hashed-secret", phone = phone)

    private fun baseRequest(
        name: String = "Ada Lovelace",
        email: String = "ada@example.com",
        phone: String? = "+1 555 123 4567",
    ) = UpdateProfileRequest(name = name, email = email, phone = phone)

    private fun feignBadRequest(body: String) = FeignException.BadRequest(
        "bad request", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    // ---- getProfile ----

    @Test
    fun getProfileReturnsMappedFields() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))

        val result = service.getProfile(1)

        assertEquals(1, result.id)
        assertEquals("Ada Lovelace", result.name)
        assertEquals("ada@example.com", result.email)
        assertEquals("+1 555 123 4567", result.phone)
    }

    @Test
    fun getProfileRejectsUnknownUser() {
        Mockito.`when`(userRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) { service.getProfile(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- updateProfile ----

    @Test
    fun updateProfileNeverExposesOrDropsPasswordHash() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.updateProfile(1, baseRequest())

        val captor = org.mockito.ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assertEquals("hashed-secret", captor.value.passwordHash)
    }

    @Test
    fun updateProfileReturnsUpdatedFieldsWithoutPasswordHash() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(Optional.empty())
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateProfile(1, baseRequest(name = "Ada L.", email = "new@example.com", phone = null))

        assertEquals("Ada L.", result.name)
        assertEquals("new@example.com", result.email)
        assertNull(result.phone)
    }

    @Test
    fun updateProfileRejectsBlankName() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser()))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.updateProfile(1, baseRequest(name = "  "))
            }
        assertEquals("name must not be blank", ex.errors["name"])
    }

    @Test
    fun updateProfileRejectsInvalidEmailFormat() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.updateProfile(1, baseRequest(email = "not-an-email"))
            }
        assertEquals(true, ex.errors.containsKey("email"))
    }

    @Test
    fun updateProfileRejectsEmailAlreadyUsedByAnotherUser() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("taken@example.com"))
            .thenReturn(Optional.of(existingUser(id = 2, email = "taken@example.com")))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.updateProfile(1, baseRequest(email = "taken@example.com"))
            }
        assertEquals("email already in use", ex.errors["email"])
    }

    @Test
    fun updateProfileAllowsKeepingOwnEmail() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser(id = 1)))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateProfile(1, baseRequest(email = "ada@example.com"))

        assertEquals("ada@example.com", result.email)
    }

    @Test
    fun updateProfileRejectsInvalidPhone() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser()))

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.updateProfile(1, baseRequest(phone = "not-a-phone!!"))
            }
        assertEquals(true, ex.errors.containsKey("phone"))
    }

    @Test
    fun updateProfileAllowsNullPhone() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateProfile(1, baseRequest(phone = null))

        assertNull(result.phone)
    }

    // ---- uploadProfilePicture ----

    @Test
    fun uploadProfilePictureSavesFileAndUpdatesUser() {
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0)).thenReturn(
            MediaResponse(
                id = 7, ownerType = "USER", ownerId = 1,
                url = "http://localhost:8080/uploads/users/1/new-key.png",
                caption = null, isPrimary = false, displayOrder = 0,
            ),
        )
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.uploadProfilePicture(1, file)

        assertEquals("http://localhost:8080/uploads/users/1/new-key.png", result.profilePictureUrl)
        val captor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assertEquals("hashed-secret", captor.value.passwordHash)
    }

    @Test
    fun uploadProfilePictureDeletesPreviousPicture() {
        val file = MockMultipartFile("file", "avatar.png", "image/png", byteArrayOf(1, 2, 3))
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(
            listOf(
                MediaResponse(
                    id = 3, ownerType = "USER", ownerId = 1,
                    url = "http://localhost:8080/uploads/users/1/old-key.png",
                    caption = null, isPrimary = false, displayOrder = 0,
                ),
            ),
        )
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0)).thenReturn(
            MediaResponse(
                id = 7, ownerType = "USER", ownerId = 1,
                url = "http://localhost:8080/uploads/users/1/new-key.png",
                caption = null, isPrimary = false, displayOrder = 0,
            ),
        )
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.uploadProfilePicture(1, file)

        Mockito.verify(mediaFeignClient).delete("USER", 1, 3)
    }

    @Test
    fun uploadProfilePictureMapsFeignBadRequestToResponseStatusException() {
        val file = MockMultipartFile("file", "notes.txt", "text/plain", byteArrayOf(1))
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(mediaFeignClient.listForOwner("USER", 1)).thenReturn(emptyList())
        Mockito.`when`(mediaFeignClient.upload("USER", 1, file, null, false, 0))
            .thenThrow(feignBadRequest("""{"message":"file must be an image (got: text/plain)"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) { service.uploadProfilePicture(1, file) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("file must be an image (got: text/plain)", ex.reason)
    }

    // ---- changePassword ----

    @Test
    fun changePasswordUpdatesHashWhenCurrentPasswordMatches() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(passwordEncoder.matches("old-pass", "hashed-secret")).thenReturn(true)
        Mockito.`when`(passwordEncoder.encode("new-password")).thenReturn("new-hashed-secret")
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.changePassword(1, ChangePasswordRequest(currentPassword = "old-pass", newPassword = "new-password"))

        val captor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assertEquals("new-hashed-secret", captor.value.passwordHash)
    }

    @Test
    fun changePasswordRejectsWrongCurrentPassword() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(passwordEncoder.matches("wrong", "hashed-secret")).thenReturn(false)

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.changePassword(1, ChangePasswordRequest(currentPassword = "wrong", newPassword = "new-password"))
            }
        assertEquals("current password is incorrect", ex.errors["currentPassword"])
    }

    @Test
    fun changePasswordRejectsShortNewPassword() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(passwordEncoder.matches("old-pass", "hashed-secret")).thenReturn(true)

        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.changePassword(1, ChangePasswordRequest(currentPassword = "old-pass", newPassword = "short"))
            }
        assertEquals(true, ex.errors.containsKey("newPassword"))
    }

    // ---- deleteAccount ----

    @Test
    fun deleteAccountSoftDeletesUser() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.deleteAccount(1)

        val captor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assertEquals(1, captor.value.id)
        assert(captor.value.deletedAt != null)
    }
}
