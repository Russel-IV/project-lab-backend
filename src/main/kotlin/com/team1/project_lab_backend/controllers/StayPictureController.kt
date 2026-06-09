package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.services.StayPictureService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/stays/{stayId}/pictures")
class StayPictureController(
    private val stayPictureService: StayPictureService
) {
    @GetMapping
    fun getPicturesForStay(@PathVariable stayId: Int): ResponseEntity<List<StayPictureResponse>> =
        ResponseEntity.ok(stayPictureService.getPicturesForStay(stayId))

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addPicture(
        @PathVariable stayId: Int,
        @RequestPart("file") file: MultipartFile,
        @RequestParam(required = false) caption: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "0") displayOrder: Int
    ): ResponseEntity<StayPictureResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(stayPictureService.addPicture(stayId, file, caption, isPrimary, displayOrder))

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updatePicture(
        @PathVariable stayId: Int,
        @PathVariable id: Int,
        @RequestPart(name = "file", required = false) file: MultipartFile?,
        @RequestParam(required = false) caption: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "0") displayOrder: Int
    ): ResponseEntity<StayPictureResponse> =
        ResponseEntity.ok(stayPictureService.updatePicture(stayId, id, file, caption, isPrimary, displayOrder))

    @DeleteMapping("/{id}")
    fun deletePicture(
        @PathVariable stayId: Int,
        @PathVariable id: Int
    ): ResponseEntity<Unit> =
        stayPictureService.deletePicture(stayId, id).let { ResponseEntity.noContent().build() }
}
