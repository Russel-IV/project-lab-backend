package com.team1.project_lab_backend.dto

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
