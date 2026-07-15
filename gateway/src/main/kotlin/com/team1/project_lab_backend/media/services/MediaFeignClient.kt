package com.team1.project_lab_backend.media.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@FeignClient(name = "media-service")
interface MediaFeignClient {

    @GetMapping("/api/v1/media")
    fun listForOwners(@RequestParam ownerType: String, @RequestParam ownerIds: List<Int>): List<MediaResponse>

    @GetMapping("/api/v1/media/{ownerType}/{ownerId}")
    fun listForOwner(@PathVariable ownerType: String, @PathVariable ownerId: Int): List<MediaResponse>

    @PostMapping("/api/v1/media/{ownerType}/{ownerId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @PathVariable ownerType: String,
        @PathVariable ownerId: Int,
        @RequestPart("file") file: MultipartFile,
        @RequestParam caption: String?,
        @RequestParam isPrimary: Boolean,
        @RequestParam displayOrder: Int,
    ): MediaResponse

    @PatchMapping("/api/v1/media/{ownerType}/{ownerId}/{id}")
    fun update(
        @PathVariable ownerType: String,
        @PathVariable ownerId: Int,
        @PathVariable id: Int,
        @RequestBody request: UpdateMediaRequest,
    ): MediaResponse

    @DeleteMapping("/api/v1/media/{ownerType}/{ownerId}/{id}")
    fun delete(@PathVariable ownerType: String, @PathVariable ownerId: Int, @PathVariable id: Int)
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
