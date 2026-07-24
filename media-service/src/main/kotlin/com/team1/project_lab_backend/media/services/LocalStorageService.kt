package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.util.Uuid7
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path

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
        val original = resolveOriginalToStore(file)
        val filename = "${Uuid7.randomUUID()}.${original.extension}"
        val key = "$folder/$filename"
        val dir = Path.of(uploadDir).toAbsolutePath().resolve(folder)
        Files.createDirectories(dir)
        Files.write(dir.resolve(filename), original.bytes)
        return key
    }

    override fun saveVariants(
        file: MultipartFile,
        folder: String,
    ): Map<Int, String> {
        val variants = file.inputStream.use { ImageResizer.resizeAll(it) }
        val dir = Path.of(uploadDir).toAbsolutePath().resolve(folder)
        Files.createDirectories(dir)
        return variants.mapValues { (width, bytes) ->
            val filename = "${Uuid7.randomUUID()}_$width.webp"
            Files.write(dir.resolve(filename), bytes)
            "$folder/$filename"
        }
    }

    override fun delete(key: String) {
        try {
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().resolve(key))
        } catch (_: Exception) {
        }
    }

    override fun toUrl(key: String): String = "${publicUrl.trimEnd('/')}/uploads/$key"
}
