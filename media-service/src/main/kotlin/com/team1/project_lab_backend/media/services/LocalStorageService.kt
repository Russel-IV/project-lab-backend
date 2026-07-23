package com.team1.project_lab_backend.media.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

/**
 * Writes to the same `uploads` Docker volume the Gateway mounts and serves static
 * uploads from (see gateway's WebConfig) — the Gateway's static resource handler
 * didn't move here, so `app.public-url` below must be the Gateway's own
 * externally-reachable URL, not this service's. A real StorageService swap-in
 * (e.g. S3, per CLAUDE.md) would make this shared-volume detail moot: object
 * storage URLs are already globally reachable without routing through Gateway.
 */
@Service
@ConditionalOnProperty(name = ["app.storage.type"], havingValue = "local", matchIfMissing = true)
class LocalStorageService(
    @Value("\${app.upload.dir}") private val uploadDir: String,
    @Value("\${app.public-url}") private val publicUrl: String,
) : StorageService {
    override fun save(
        file: MultipartFile,
        folder: String,
    ): String {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val filename = "${UUID.randomUUID()}.$ext"
        val key = "$folder/$filename"
        val dir = Path.of(uploadDir).toAbsolutePath().resolve(folder)
        Files.createDirectories(dir)
        file.inputStream.use { Files.copy(it, dir.resolve(filename)) }
        return key
    }

    override fun saveThumbnail(
        file: MultipartFile,
        folder: String,
    ): String? {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val resized = file.inputStream.use { ImageResizer.resize(it, ext) } ?: return null
        val filename = "${UUID.randomUUID()}_thumb.$ext"
        val key = "$folder/$filename"
        val dir = Path.of(uploadDir).toAbsolutePath().resolve(folder)
        Files.createDirectories(dir)
        Files.write(dir.resolve(filename), resized)
        return key
    }

    override fun delete(key: String) {
        try {
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().resolve(key))
        } catch (_: Exception) {
        }
    }

    override fun toUrl(key: String): String = "${publicUrl.trimEnd('/')}/uploads/$key"
}
