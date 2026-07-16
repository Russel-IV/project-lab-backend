package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ProfileResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "identity-service", contextId = "profileFeignClient")
interface ProfileFeignClient {
    @GetMapping("/internal/profile/{userId}")
    fun getProfile(
        @PathVariable userId: Int,
    ): ProfileResponse

    @PatchMapping("/internal/profile/{userId}")
    fun updateProfile(
        @PathVariable userId: Int,
        @RequestBody request: ProfileUpdateRequest,
    ): ProfileResponse

    @PatchMapping("/internal/profile/{userId}/picture-url")
    fun updatePictureUrl(
        @PathVariable userId: Int,
        @RequestBody request: UpdatePictureUrlRequest,
    ): ProfileResponse

    @PatchMapping("/internal/profile/{userId}/password")
    fun changePassword(
        @PathVariable userId: Int,
        @RequestBody request: PasswordChangeRequest,
    )

    @DeleteMapping("/internal/profile/{userId}")
    fun deleteAccount(
        @PathVariable userId: Int,
    )
}

data class ProfileUpdateRequest(val name: String, val email: String, val phone: String?)

data class UpdatePictureUrlRequest(val url: String)

data class PasswordChangeRequest(val currentPassword: String, val newPassword: String)
