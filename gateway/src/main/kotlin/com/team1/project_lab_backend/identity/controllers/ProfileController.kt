package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.ChangePasswordRequest
import com.team1.project_lab_backend.identity.dto.ProfileResponse
import com.team1.project_lab_backend.identity.dto.UpdateProfileRequest
import com.team1.project_lab_backend.identity.services.ProfileService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profile")
class ProfileController(
    private val profileService: ProfileService,
) {
    @GetMapping
    suspend fun getProfile(): ProfileResponse {
        val currentUser = requireAuthenticated()
        return profileService.getProfile(currentUser.id)
    }

    @PatchMapping
    suspend fun updateProfile(
        @RequestBody request: UpdateProfileRequest,
    ): ProfileResponse {
        val currentUser = requireAuthenticated()
        return profileService.updateProfile(currentUser.id, request)
    }

    @PostMapping("/picture", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadProfilePicture(
        @RequestPart("file") file: FilePart,
    ): ProfileResponse {
        val currentUser = requireAuthenticated()
        return profileService.uploadProfilePicture(currentUser.id, file)
    }

    @PatchMapping("/password")
    suspend fun changePassword(
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        val currentUser = requireAuthenticated()
        profileService.changePassword(currentUser.id, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    suspend fun deleteAccount(): ResponseEntity<Void> {
        val currentUser = requireAuthenticated()
        profileService.deleteAccount(currentUser.id)
        return ResponseEntity.noContent().build()
    }
}
