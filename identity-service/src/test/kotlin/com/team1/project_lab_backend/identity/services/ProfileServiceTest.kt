package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class ProfileServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private val service = ProfileService(userRepository, passwordEncoder)

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

    @Test
    fun getProfileReturnsMappedFields() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        val result = service.getProfile(1)
        assertEquals("Ada Lovelace", result.name)
        assertEquals("ada@example.com", result.email)
    }

    @Test
    fun getProfileRejectsUnknownUser() {
        Mockito.`when`(userRepository.findById(99)).thenReturn(Optional.empty())
        val ex = assertThrows(ResponseStatusException::class.java) { service.getProfile(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateProfileNeverExposesOrDropsPasswordHash() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.findByEmailAndDeletedAtIsNull("ada@example.com")).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.updateProfile(1, baseRequest())

        val captor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assertEquals("hashed-secret", captor.value.passwordHash)
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
    fun updateProfilePictureUrlSavesUrl() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateProfilePictureUrl(1, "http://localhost:8080/uploads/users/1/x.png")

        assertEquals("http://localhost:8080/uploads/users/1/x.png", result.profilePictureUrl)
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
    fun deleteAccountSoftDeletesUser() {
        Mockito.`when`(userRepository.findById(1)).thenReturn(Optional.of(existingUser()))
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { it.arguments[0] }

        service.deleteAccount(1)

        val captor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(captor.capture())
        assert(captor.value.deletedAt != null)
    }
}
