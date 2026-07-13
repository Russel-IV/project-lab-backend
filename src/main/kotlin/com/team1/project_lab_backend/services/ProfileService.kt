package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ChangePasswordRequest
import com.team1.project_lab_backend.dto.ProfileResponse
import com.team1.project_lab_backend.dto.UpdateProfileRequest
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.orNotFound
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
    private val storageService: StorageService,
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

    @Transactional
    fun uploadProfilePicture(userId: Int, file: MultipartFile): ProfileResponse {
        val existing = userRepository.findById(userId).orNotFound("user not found")
        validateImageFile(file)
        val oldPictureUrl = existing.profilePictureUrl

        val key = storageService.save(file, "users/$userId")
        try {
            val saved = userRepository.save(
                User(
                    id = userId,
                    name = existing.name,
                    email = existing.email,
                    passwordHash = existing.passwordHash,
                    phone = existing.phone,
                    profilePictureUrl = key,
                ),
            )
            oldPictureUrl?.let { storageService.delete(it) }
            return saved.toProfileResponse()
        } catch (e: Exception) {
            runCatching { storageService.delete(key) }
            throw e
        }
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

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty")
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "file must be an image (got: $contentType)")
        }
        val ext = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: ""
        if (ext !in ALLOWED_EXTENSIONS) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported image extension: .$ext")
        }
    }

    private fun User.toProfileResponse() = ProfileResponse(
        id = id,
        name = name,
        email = email,
        phone = phone,
        profilePictureUrl = profilePictureUrl?.let { storageService.toUrl(it) },
    )

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "avif")
    }
}
