package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfilePictureUrlRequest
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.services.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/profile/{userId}")
class ProfileController(private val profileService: ProfileService) {
    @GetMapping
    fun getProfile(
        @PathVariable userId: Int,
    ): ProfileResponse = profileService.getProfile(userId)

    @PatchMapping
    fun updateProfile(
        @PathVariable userId: Int,
        @RequestBody request: UpdateProfileRequest,
    ): ProfileResponse = profileService.updateProfile(userId, request)

    @PatchMapping("/picture-url")
    fun updatePictureUrl(
        @PathVariable userId: Int,
        @RequestBody request: UpdateProfilePictureUrlRequest,
    ): ProfileResponse = profileService.updateProfilePictureUrl(userId, request.url)

    @PatchMapping("/password")
    fun changePassword(
        @PathVariable userId: Int,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        profileService.changePassword(userId, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun deleteAccount(
        @PathVariable userId: Int,
    ): ResponseEntity<Void> {
        profileService.deleteAccount(userId)
        return ResponseEntity.noContent().build()
    }
}
