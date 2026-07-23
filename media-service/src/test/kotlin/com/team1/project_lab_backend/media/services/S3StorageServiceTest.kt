package com.team1.project_lab_backend.media.services

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

class S3StorageServiceTest {
    private val s3Client = Mockito.mock(S3Client::class.java)

    private val service = S3StorageService(
        s3Client = s3Client,
        bucket = "test-bucket",
        region = "us-east-1",
        endpoint = "",
    )

    private val serviceWithCustomEndpoint = S3StorageService(
        s3Client = s3Client,
        bucket = "test-bucket",
        region = "us-east-1",
        endpoint = "http://localhost:4566",
    )

    @Test
    fun saveUploadsFileToS3AndReturnsKey() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", ByteArray(8) { 0 })

        val key = service.save(file, "stays/10")

        assertTrue(key.startsWith("stays/10/"))
        assertTrue(key.endsWith(".jpg"))

        val captor = ArgumentCaptor.forClass(PutObjectRequest::class.java)
        Mockito.verify(s3Client).putObject(captor.capture(), Mockito.any(RequestBody::class.java))

        val putRequest = captor.value
        assertEquals("test-bucket", putRequest.bucket())
        assertEquals(key, putRequest.key())
        assertEquals("image/jpeg", putRequest.contentType())
    }

    @Test
    fun deleteIssuesDeleteObjectRequest() {
        val key = "stays/10/photo.jpg"

        service.delete(key)

        val captor = ArgumentCaptor.forClass(DeleteObjectRequest::class.java)
        Mockito.verify(s3Client).deleteObject(captor.capture())

        val deleteRequest = captor.value
        assertEquals("test-bucket", deleteRequest.bucket())
        assertEquals(key, deleteRequest.key())
    }

    @Test
    fun toUrlFormatsStandardS3Url() {
        val key = "stays/10/photo.jpg"
        val url = service.toUrl(key)

        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/stays/10/photo.jpg", url)
    }

    @Test
    fun toUrlFormatsCustomEndpointUrlWhenSet() {
        val key = "stays/10/photo.jpg"
        val url = serviceWithCustomEndpoint.toUrl(key)

        assertEquals("http://localhost:4566/test-bucket/stays/10/photo.jpg", url)
    }
}
