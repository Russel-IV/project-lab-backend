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

internal data class OriginalToStore(
    val bytes: ByteArray,
    val extension: String,
    val contentType: String,
)

/**
 * Shared by both StorageService implementations' save(): pass an already-WebP upload
 * through untouched, convert everything else to WebP, and fall back to the original
 * bytes/extension/content-type if conversion isn't possible (undecodable input like
 * avif, or WebP encoding unavailable on this platform) — never fail the upload over it.
 */
internal fun resolveOriginalToStore(file: MultipartFile): OriginalToStore {
    val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
    if (ext.equals("webp", ignoreCase = true)) {
        return OriginalToStore(file.bytes, "webp", "image/webp")
    }

    val converted = file.inputStream.use { ImageResizer.convertToWebp(it) }
    return if (converted != null) {
        OriginalToStore(converted, "webp", "image/webp")
    } else {
        OriginalToStore(file.bytes, ext, file.contentType ?: "application/octet-stream")
    }
}
