package com.team1.project_lab_backend.media.services

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class S3StorageServiceTest {
    private val s3Client = Mockito.mock(S3Client::class.java)

    private val service =
        S3StorageService(
            s3Client = s3Client,
            bucket = "test-bucket",
            region = "us-east-1",
            endpoint = "",
        )

    private val serviceWithCustomEndpoint =
        S3StorageService(
            s3Client = s3Client,
            bucket = "test-bucket",
            region = "us-east-1",
            endpoint = "http://localhost:4566",
        )

    private fun jpegBytes(
        width: Int = 300,
        height: Int = 200,
    ): ByteArray {
        val source = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        return ByteArrayOutputStream().also { ImageIO.write(source, "jpg", it) }.toByteArray()
    }

    private fun capturePutObject(): Pair<PutObjectRequest, ByteArray> {
        val requestCaptor = ArgumentCaptor.forClass(PutObjectRequest::class.java)
        val bodyCaptor = ArgumentCaptor.forClass(RequestBody::class.java)
        Mockito.verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture())
        return requestCaptor.value to bodyCaptor.value.contentStreamProvider().newStream().readAllBytes()
    }

    @Test
    fun saveConvertsUploadToWebpAndReturnsKey() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes())

        val key = service.save(file, "stays/10")

        assertTrue(key.startsWith("stays/10/"))
        assertTrue(key.endsWith(".webp"))

        val (putRequest, _) = capturePutObject()
        assertEquals("test-bucket", putRequest.bucket())
        assertEquals(key, putRequest.key())
        assertEquals("image/webp", putRequest.contentType())
        assertEquals("public, max-age=31536000, immutable", putRequest.cacheControl())
    }

    @Test
    fun saveStoresAlreadyWebpUploadUnmodified() {
        val webpBytes = ByteArray(16) { it.toByte() }
        val file = MockMultipartFile("file", "test.webp", "image/webp", webpBytes)

        val key = service.save(file, "stays/10")

        assertTrue(key.endsWith(".webp"))
        val (putRequest, body) = capturePutObject()
        assertEquals("image/webp", putRequest.contentType())
        assertArrayEquals(webpBytes, body)
    }

    @Test
    fun saveFallsBackToOriginalWhenInputCannotBeDecoded() {
        val garbage = ByteArray(8) { 0 }
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", garbage)

        val key = service.save(file, "stays/10")

        assertTrue(key.endsWith(".jpg"))
        val (putRequest, body) = capturePutObject()
        assertEquals("image/jpeg", putRequest.contentType())
        assertArrayEquals(garbage, body)
    }

    @Test
    fun saveVariantsUploadsOneObjectPerGeneratedWidth() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes())

        val variants = service.saveVariants(file, "stays/10")

        assertEquals(setOf(248), variants.keys)
        val (putRequest, _) = capturePutObject()
        assertEquals("stays/10", putRequest.key().substringBeforeLast('/'))
        assertTrue(putRequest.key().endsWith("_248.webp"))
        assertEquals("image/webp", putRequest.contentType())
        assertEquals("public, max-age=31536000, immutable", putRequest.cacheControl())
    }

    @Test
    fun deleteIssuesDeleteObjectRequest() {
        val key = "stays/10/photo.webp"

        service.delete(key)

        val captor = ArgumentCaptor.forClass(DeleteObjectRequest::class.java)
        Mockito.verify(s3Client).deleteObject(captor.capture())

        val deleteRequest = captor.value
        assertEquals("test-bucket", deleteRequest.bucket())
        assertEquals(key, deleteRequest.key())
    }

    @Test
    fun toUrlFormatsStandardS3Url() {
        val key = "stays/10/photo.webp"
        val url = service.toUrl(key)

        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/stays/10/photo.webp", url)
    }

    @Test
    fun toUrlFormatsCustomEndpointUrlWhenSet() {
        val key = "stays/10/photo.webp"
        val url = serviceWithCustomEndpoint.toUrl(key)

        assertEquals("http://localhost:4566/test-bucket/stays/10/photo.webp", url)
    }
}
