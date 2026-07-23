package com.team1.project_lab_backend.media.services

import org.springframework.web.multipart.MultipartFile

interface StorageService {
    fun save(
        file: MultipartFile,
        folder: String,
    ): String

    /** Returns each generated variant's storage key by width, e.g. {1024: "...", 512: "...", 248: "..."}. */
    fun saveVariants(
        file: MultipartFile,
        folder: String,
    ): Map<Int, String>

    fun delete(key: String)

    fun toUrl(key: String): String
}
