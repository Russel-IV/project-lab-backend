package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.dto.toProfileResponse
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val PHONE_REGEX = Regex("^[0-9+()\\-\\s]{7,32}$")

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional(readOnly = true)
    fun getProfile(userId: Int): ProfileResponse =
        userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }
            .toProfileResponse()

    @Transactional
    fun updateProfile(
        userId: Int,
        request: UpdateProfileRequest,
    ): ProfileResponse {
        val existing =
            userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }

        val errors = mutableMapOf<String, String>()
        if (request.name.isBlank()) {
            errors["name"] = "name must not be blank"
        }
        when {
            request.email.isBlank() -> errors["email"] = "email must not be blank"
            !EMAIL_REGEX.matches(request.email) -> errors["email"] = "email is not a valid email address"
            userRepository.findByEmailAndDeletedAtIsNull(request.email).map { it.id }.orElse(userId) != userId ->
                errors["email"] = "email already in use"
        }
        if (!request.phone.isNullOrBlank() && !PHONE_REGEX.matches(request.phone)) {
            errors["phone"] = "phone is not a valid phone number"
        }
        if (errors.isNotEmpty()) throw FieldValidationException(errors)

        return userRepository.save(
            User(
                id = userId,
                publicId = existing.publicId,
                name = request.name,
                email = request.email,
                passwordHash = existing.passwordHash,
                phone = request.phone,
                profilePictureUrl = existing.profilePictureUrl,
            ),
        ).toProfileResponse()
    }

    @Transactional
    fun updateProfilePictureUrl(
        userId: Int,
        url: String,
    ): ProfileResponse {
        val existing =
            userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }
        return userRepository.save(
            User(
                id = userId,
                publicId = existing.publicId,
                name = existing.name,
                email = existing.email,
                passwordHash = existing.passwordHash,
                phone = existing.phone,
                profilePictureUrl = url,
            ),
        ).toProfileResponse()
    }

    @Transactional
    fun changePassword(
        userId: Int,
        request: ChangePasswordRequest,
    ) {
        val existing =
            userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }

        val errors = mutableMapOf<String, String>()
        if (!passwordEncoder.matches(request.currentPassword, existing.passwordHash)) {
            errors["currentPassword"] = "current password is incorrect"
        }
        if (request.newPassword.length < 8) {
            errors["newPassword"] = "new password must be at least 8 characters"
        }
        if (errors.isNotEmpty()) throw FieldValidationException(errors)

        userRepository.save(
            User(
                id = userId,
                publicId = existing.publicId,
                name = existing.name,
                email = existing.email,
                passwordHash = passwordEncoder.encode(request.newPassword),
                phone = existing.phone,
                profilePictureUrl = existing.profilePictureUrl,
            ),
        )
    }

    @Transactional
    fun deleteAccount(userId: Int) {
        val existing =
            userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }
        userRepository.save(
            User(
                id = userId,
                publicId = existing.publicId,
                name = existing.name,
                email = existing.email,
                passwordHash = existing.passwordHash,
                phone = existing.phone,
                profilePictureUrl = existing.profilePictureUrl,
                deletedAt = LocalDateTime.now(),
            ),
        )
    }
}
