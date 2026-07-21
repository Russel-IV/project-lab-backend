package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.media.services.MediaFeignClient
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.webClientErrorMessage
import com.team1.project_lab_backend.util.webClientFieldErrors
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): profile validation/persistence now lives in
 * identity-service, reached via profileFeignClient. Picture upload is the one
 * exception — the Gateway is where the incoming file part actually lands, and it
 * already has mediaFeignClient (docs/adr/0003, Phase 3), so it uploads directly and
 * only ever sends identity-service the resolved URL to persist.
 */
@Service
class ProfileService(
    private val profileFeignClient: ProfileFeignClient,
    private val mediaFeignClient: MediaFeignClient,
) {
    suspend fun getProfile(userId: Int): ProfileResponse =
        try {
            profileFeignClient.getProfile(userId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

    suspend fun updateProfile(
        userId: Int,
        request: UpdateProfileRequest,
    ): ProfileResponse =
        try {
            profileFeignClient.updateProfile(
                userId,
                ProfileUpdateRequest(name = request.name, email = request.email, phone = request.phone),
            )
        } catch (e: WebClientResponseException.UnprocessableContent) {
            throw FieldValidationException(webClientFieldErrors(e) ?: emptyMap())
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

    /**
     * media-service owns validation/storage/the one-primary-per-owner invariant now
     * (docs/adr/0003) — a user only ever has one picture, so isPrimary is irrelevant
     * here (always false) and just needs media-service's per-owner uniqueness to
     * never trigger. Upload happens before deleting the old picture (not after) so a
     * failed upload never leaves the user with no picture at all.
     */
    suspend fun uploadProfilePicture(
        userId: Int,
        file: FilePart,
    ): ProfileResponse {
        val previous = mediaFeignClient.listForOwner("USER", userId)

        val uploaded =
            try {
                mediaFeignClient.upload("USER", userId, file, caption = null, isPrimary = false, displayOrder = 0)
            } catch (e: WebClientResponseException.BadRequest) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid image")
            }

        val result =
            try {
                profileFeignClient.updatePictureUrl(userId, UpdatePictureUrlRequest(uploaded.url))
            } catch (e: WebClientResponseException.NotFound) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
            }
        previous.forEach { old -> runCatching { mediaFeignClient.delete("USER", userId, old.id) } }
        return result
    }

    suspend fun changePassword(
        userId: Int,
        request: ChangePasswordRequest,
    ) {
        try {
            profileFeignClient.changePassword(
                userId,
                PasswordChangeRequest(currentPassword = request.currentPassword, newPassword = request.newPassword),
            )
        } catch (e: WebClientResponseException.UnprocessableContent) {
            throw FieldValidationException(webClientFieldErrors(e) ?: emptyMap())
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
    }

    suspend fun deleteAccount(userId: Int) {
        try {
            profileFeignClient.deleteAccount(userId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
    }
}
