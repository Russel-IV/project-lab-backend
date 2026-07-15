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
 * profilePictureUrl is stored as the fully-resolved public URL media-service's
 * upload response returned (docs/adr/0003, Phase 3) — Identity has no StorageService
 * of its own to resolve a key, so there's nothing left to transform here.
 */
fun User.toProfileResponse() = ProfileResponse(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profilePictureUrl = profilePictureUrl,
)
