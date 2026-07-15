package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.dto.toProfileResponse
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.media.services.MediaFeignClient
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.feignErrorMessage
import com.team1.project_lab_backend.util.orNotFound
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val PHONE_REGEX = Regex("^[0-9+()\\-\\s]{7,32}$")

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val mediaFeignClient: MediaFeignClient,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional(readOnly = true)
    fun getProfile(userId: Int): ProfileResponse =
        userRepository.findById(userId).orNotFound("user not found").toProfileResponse()

    @Transactional
    fun updateProfile(userId: Int, request: UpdateProfileRequest): ProfileResponse {
        val existing = userRepository.findById(userId).orNotFound("user not found")

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
                name = request.name,
                email = request.email,
                passwordHash = existing.passwordHash,
                phone = request.phone,
                profilePictureUrl = existing.profilePictureUrl,
            ),
        ).toProfileResponse()
    }

    /**
     * media-service owns validation/storage/the one-primary-per-owner invariant now
     * (docs/adr/0003) — a user only ever has one picture, so isPrimary is irrelevant
     * here (always false) and just needs media-service's per-owner uniqueness to
     * never trigger. Upload happens before deleting the old picture (not after) so a
     * failed upload never leaves the user with no picture at all.
     */
    @Transactional
    fun uploadProfilePicture(userId: Int, file: MultipartFile): ProfileResponse {
        val existing = userRepository.findById(userId).orNotFound("user not found")
        val previous = mediaFeignClient.listForOwner("USER", userId)

        val uploaded = try {
            mediaFeignClient.upload("USER", userId, file, caption = null, isPrimary = false, displayOrder = 0)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid image")
        }

        val saved = userRepository.save(
            User(
                id = userId,
                name = existing.name,
                email = existing.email,
                passwordHash = existing.passwordHash,
                phone = existing.phone,
                profilePictureUrl = uploaded.url,
            ),
        )
        previous.forEach { old -> runCatching { mediaFeignClient.delete("USER", userId, old.id) } }
        return saved.toProfileResponse()
    }

    @Transactional
    fun changePassword(userId: Int, request: ChangePasswordRequest) {
        val existing = userRepository.findById(userId).orNotFound("user not found")

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
        val existing = userRepository.findById(userId).orNotFound("user not found")
        userRepository.save(
            User(
                id = userId,
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
