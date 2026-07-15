package com.team1.project_lab_backend.media.services

import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun save(file: MultipartFile, folder: String): String
    fun delete(key: String)
    fun toUrl(key: String): String
}
