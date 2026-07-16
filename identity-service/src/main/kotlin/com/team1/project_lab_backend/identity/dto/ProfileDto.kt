package com.team1.project_lab_backend.identity.dto

import com.team1.project_lab_backend.identity.models.User

data class ProfileResponse(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val profilePictureUrl: String?,
)

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String?,
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

/**
 * The Gateway does the actual picture upload (it's the one that receives the
 * MultipartFile and already has a MediaFeignClient from Phase 3) and only ever
 * sends this service the resolved public URL media-service returned, to persist.
 */
data class UpdateProfilePictureUrlRequest(
    val url: String,
)

fun User.toProfileResponse() =
    ProfileResponse(
        id = id,
        name = name,
        email = email,
        phone = phone,
        profilePictureUrl = profilePictureUrl,
    )
