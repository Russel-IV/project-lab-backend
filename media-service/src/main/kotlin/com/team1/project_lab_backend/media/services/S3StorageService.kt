package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.util.Uuid7
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * Direct backend AWS S3 object storage implementation of [StorageService].
 * Activated when `app.storage.type=s3`.
 */
@Service
@ConditionalOnProperty(name = ["app.storage.type"], havingValue = "s3")
class S3StorageService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.s3.region:us-east-1}") private val region: String,
    @Value("\${cloud.aws.s3.endpoint:}") private val endpoint: String,
) : StorageService {
    override fun save(
        file: MultipartFile,
        folder: String,
    ): String {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val filename = "${Uuid7.randomUUID()}.$ext"
        val key = "$folder/$filename"

        val putObjectRequest =
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType ?: "application/octet-stream")
                .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))
        return key
    }

    override fun saveVariants(
        file: MultipartFile,
        folder: String,
    ): Map<Int, String> {
        val ext = file.originalFilename?.substringAfterLast('.', "bin")?.ifBlank { "bin" } ?: "bin"
        val variants = file.inputStream.use { ImageResizer.resizeAll(it, ext) }
        return variants.mapValues { (width, bytes) ->
            val key = "$folder/${Uuid7.randomUUID()}_$width.$ext"
            val putObjectRequest =
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.contentType ?: "application/octet-stream")
                    .build()
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes))
            key
        }
    }

    override fun delete(key: String) {
        try {
            val deleteObjectRequest =
                DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            s3Client.deleteObject(deleteObjectRequest)
        } catch (_: Exception) {
        }
    }

    override fun toUrl(key: String): String {
        return if (endpoint.isNotBlank()) {
            "${endpoint.trimEnd('/')}/$bucket/$key"
        } else {
            "https://$bucket.s3.$region.amazonaws.com/$key"
        }
    }
}
