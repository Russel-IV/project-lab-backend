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
import java.util.concurrent.CompletableFuture

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
        val original = resolveOriginalToStore(file)
        val filename = "${Uuid7.randomUUID()}.${original.extension}"
        val key = "$folder/$filename"

        val putObjectRequest =
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(original.contentType)
                .cacheControl(CACHE_CONTROL)
                .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(original.bytes))
        return key
    }

    override fun saveVariants(
        file: MultipartFile,
        folder: String,
    ): Map<Int, String> {
        val variants = file.inputStream.use { ImageResizer.resizeAll(it) }
        // Resizing/encoding above is CPU-bound and stays sequential; only the actual
        // network upload of each already-computed variant runs concurrently, since
        // each PUT is otherwise a full blocking round-trip to S3 for no CPU benefit.
        val uploads =
            variants.map { (width, bytes) ->
                val key = "$folder/${Uuid7.randomUUID()}_$width.webp"
                val putObjectRequest =
                    PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("image/webp")
                        .cacheControl(CACHE_CONTROL)
                        .build()
                CompletableFuture.supplyAsync {
                    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes))
                    width to key
                }
            }
        return uploads.map { it.join() }.toMap()
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

    companion object {
        // Every key is a fresh UUIDv7 (see save()/saveVariants()) and is never overwritten in
        // place, so objects are safe to cache aggressively and indefinitely.
        private const val CACHE_CONTROL = "public, max-age=31536000, immutable"
    }
}
