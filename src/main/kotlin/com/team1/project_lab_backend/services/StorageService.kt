package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.ProfileResponse
import com.team1.project_lab_backend.models.User
import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun save(file: MultipartFile, folder: String): String
    fun delete(key: String)
    fun toUrl(key: String): String
}

fun User.toProfileResponse(storageService: StorageService) = ProfileResponse(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profilePictureUrl = profilePictureUrl?.let { storageService.toUrl(it) },
)
