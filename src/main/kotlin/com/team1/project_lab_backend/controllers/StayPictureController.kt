package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.services.StayPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/stays/{stayId}/pictures")
class StayPictureController(
    private val stayPictureService: StayPictureService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addPicture(
        @PathVariable stayId: Int,
        @RequestPart("file") file: MultipartFile,
        @RequestParam(required = false) caption: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "0") displayOrder: Int,
    ): ResponseEntity<StayPictureResponse> {
        requireAuthenticated()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(stayPictureService.addPicture(stayId, file, caption, isPrimary, displayOrder))
    }
}
