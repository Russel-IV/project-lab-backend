package com.team1.project_lab_backend.services

import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun save(file: MultipartFile, stayId: Int): String
    fun delete(key: String)
    fun toUrl(key: String): String
}
