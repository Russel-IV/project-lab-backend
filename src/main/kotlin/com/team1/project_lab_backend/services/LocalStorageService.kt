package com.team1.project_lab_backend.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
class LocalStorageService(
    @Value("\${app.upload.dir}") private val uploadDir: String,
) : StorageService {

    override fun save(file: MultipartFile, stayId: Int): String {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val filename = "${UUID.randomUUID()}.$ext"
        val key = "stays/$stayId/$filename"
        val dir = Path.of(uploadDir).toAbsolutePath().resolve("stays/$stayId")
        Files.createDirectories(dir)
        file.inputStream.use { Files.copy(it, dir.resolve(filename)) }
        return key
    }

    override fun delete(key: String) {
        try {
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().resolve(key))
        } catch (_: Exception) {}
    }

    override fun toUrl(key: String): String = "/uploads/$key"
}
