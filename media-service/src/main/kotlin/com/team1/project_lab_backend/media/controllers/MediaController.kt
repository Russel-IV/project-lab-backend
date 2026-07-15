package com.team1.project_lab_backend.media.controllers

import com.team1.project_lab_backend.media.dto.MediaResponse
import com.team1.project_lab_backend.media.dto.UpdateMediaRequest
import com.team1.project_lab_backend.media.models.MediaOwnerType
import com.team1.project_lab_backend.media.services.MediaService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/media")
class MediaController(private val mediaService: MediaService) {

    @GetMapping
    fun listForOwners(@RequestParam ownerType: String, @RequestParam ownerIds: List<Int>): List<MediaResponse> =
        mediaService.listForOwners(parseOwnerType(ownerType), ownerIds)

    @GetMapping("/{ownerType}/{ownerId}")
    fun listForOwner(@PathVariable ownerType: String, @PathVariable ownerId: Int): List<MediaResponse> =
        mediaService.listForOwner(parseOwnerType(ownerType), ownerId)

    @PostMapping("/{ownerType}/{ownerId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @PathVariable ownerType: String,
        @PathVariable ownerId: Int,
        @RequestPart("file") file: MultipartFile,
        @RequestParam(required = false) caption: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "0") displayOrder: Int,
    ): ResponseEntity<MediaResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(mediaService.addMedia(parseOwnerType(ownerType), ownerId, file, caption, isPrimary, displayOrder))

    @PatchMapping("/{ownerType}/{ownerId}/{id}")
    fun update(
        @PathVariable ownerType: String,
        @PathVariable ownerId: Int,
        @PathVariable id: Int,
        @RequestBody request: UpdateMediaRequest,
    ): MediaResponse = mediaService.updateMedia(parseOwnerType(ownerType), ownerId, id, request)

    @DeleteMapping("/{ownerType}/{ownerId}/{id}")
    fun delete(@PathVariable ownerType: String, @PathVariable ownerId: Int, @PathVariable id: Int) {
        mediaService.deleteMedia(parseOwnerType(ownerType), ownerId, id)
    }

    private fun parseOwnerType(raw: String): MediaOwnerType =
        try {
            MediaOwnerType.valueOf(raw.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid ownerType: $raw")
        }
}
