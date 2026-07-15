package com.team1.project_lab_backend.identity.dto

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.media.services.StorageService

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

fun User.toProfileResponse(storageService: StorageService) = ProfileResponse(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profilePictureUrl = profilePictureUrl?.let { storageService.toUrl(it) },
)
