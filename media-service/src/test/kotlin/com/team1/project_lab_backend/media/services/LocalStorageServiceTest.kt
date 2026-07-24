package com.team1.project_lab_backend.media.services

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

class LocalStorageServiceTest {
    @TempDir
    lateinit var tempDir: Path

    private val service by lazy {
        LocalStorageService(uploadDir = tempDir.toString(), publicUrl = "http://localhost:8080")
    }

    private fun jpegBytes(
        width: Int = 300,
        height: Int = 200,
    ): ByteArray {
        val source = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        return ByteArrayOutputStream().also { ImageIO.write(source, "jpg", it) }.toByteArray()
    }

    @Test
    fun saveConvertsUploadToWebpFile() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes())

        val key = service.save(file, "stays/10")

        assertTrue(key.startsWith("stays/10/"))
        assertTrue(key.endsWith(".webp"))
        assertTrue(Files.exists(tempDir.resolve(key)))
    }

    @Test
    fun saveStoresAlreadyWebpUploadUnmodified() {
        val webpBytes = ByteArray(16) { it.toByte() }
        val file = MockMultipartFile("file", "test.webp", "image/webp", webpBytes)

        val key = service.save(file, "stays/10")

        assertTrue(key.endsWith(".webp"))
        assertArrayEquals(webpBytes, Files.readAllBytes(tempDir.resolve(key)))
    }

    @Test
    fun saveFallsBackToOriginalWhenInputCannotBeDecoded() {
        val garbage = ByteArray(8) { 0 }
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", garbage)

        val key = service.save(file, "stays/10")

        assertTrue(key.endsWith(".jpg"))
        assertArrayEquals(garbage, Files.readAllBytes(tempDir.resolve(key)))
    }

    @Test
    fun saveVariantsWritesWebpFilesForEachGeneratedWidth() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes())

        val variants = service.saveVariants(file, "stays/10")

        assertEquals(setOf(248), variants.keys)
        val key = variants.getValue(248)
        assertTrue(key.endsWith("_248.webp"))
        assertTrue(Files.exists(tempDir.resolve(key)))
    }

    @Test
    fun deleteRemovesFile() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes())
        val key = service.save(file, "stays/10")
        assertTrue(Files.exists(tempDir.resolve(key)))

        service.delete(key)

        assertTrue(Files.notExists(tempDir.resolve(key)))
    }

    @Test
    fun toUrlPrependsPublicUrlAndUploadsPrefix() {
        assertEquals("http://localhost:8080/uploads/stays/10/photo.webp", service.toUrl("stays/10/photo.webp"))
    }
}
