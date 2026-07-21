package com.team1.project_lab_backend.media.services

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class MediaFeignClient(
    @Qualifier("mediaServiceWebClient") private val webClient: WebClient,
) {

    suspend fun listForOwners(
        ownerType: String,
        ownerIds: List<Int>,
    ): List<MediaResponse> =
        webClient.get()
            .uri { b ->
                b.path("/api/v1/media").queryParam("ownerType", ownerType).queryParam("ownerIds", *ownerIds.toTypedArray()).build()
            }
            .retrieve()
            .awaitBody()

    suspend fun listForOwner(
        ownerType: String,
        ownerId: Int,
    ): List<MediaResponse> =
        webClient.get().uri("/api/v1/media/{ownerType}/{ownerId}", ownerType, ownerId).retrieve().awaitBody()

    suspend fun upload(
        ownerType: String,
        ownerId: Int,
        file: FilePart,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
    ): MediaResponse {
        val body = MultipartBodyBuilder()
        val filePart = body.asyncPart("file", file.content(), DataBuffer::class.java).filename(file.filename())
        file.headers().contentType?.let { filePart.contentType(it) }
        body.asyncPart("file", file.content(), DataBuffer::class.java).filename(file.filename())
        if (caption != null) body.part("caption", caption)
        body.part("isPrimary", isPrimary)
        body.part("displayOrder", displayOrder)
        return webClient.post()
            .uri("/api/v1/media/{ownerType}/{ownerId}", ownerType, ownerId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body.build()))
            .retrieve()
            .awaitBody()
    }

    suspend fun update(
        ownerType: String,
        ownerId: Int,
        id: Int,
        request: UpdateMediaRequest,
    ): MediaResponse =
        webClient.patch()
            .uri("/api/v1/media/{ownerType}/{ownerId}/{id}", ownerType, ownerId, id)
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun delete(
        ownerType: String,
        ownerId: Int,
        id: Int,
    ) {
        webClient.delete().uri("/api/v1/media/{ownerType}/{ownerId}/{id}", ownerType, ownerId, id).retrieve().awaitBodilessEntity()
    }
}

data class MediaResponse(
    val id: Int,
    val ownerType: String,
    val ownerId: Int,
    val url: String,
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)

data class UpdateMediaRequest(
    val caption: String?,
    val isPrimary: Boolean,
    val displayOrder: Int,
)
