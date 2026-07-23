package com.team1.project_lab_backend.media.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import java.net.URI

/**
 * Configures the AWS SDK v2 [S3Client] bean when app.storage.type=s3.
 */
@Configuration
@ConditionalOnProperty(name = ["app.storage.type"], havingValue = "s3")
class S3Config(
    @Value("\${cloud.aws.s3.region:us-east-1}") private val region: String,
    @Value("\${cloud.aws.s3.endpoint:}") private val endpoint: String,
) {
    @Bean
    fun s3Client(): S3Client {
        val builder: S3ClientBuilder = S3Client.builder()
            .region(Region.of(region))

        if (endpoint.isNotBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
        }

        return builder.build()
    }
}
