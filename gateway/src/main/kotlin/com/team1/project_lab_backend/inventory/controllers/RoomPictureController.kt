package com.team1.project_lab_backend.inventory.controllers

import com.team1.project_lab_backend.inventory.services.RoomService
import com.team1.project_lab_backend.media.dto.RoomPictureResponse
import com.team1.project_lab_backend.media.services.RoomPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

/**
 * Lives in inventory, not media, same rationale as RoomPictureResolver: ownership is
 * checked here via RoomService before delegating multipart upload to media.
 */
@RestController
@RequestMapping("/api/v1/rooms/{roomId}/pictures")
class RoomPictureController(
    private val roomPictureService: RoomPictureService,
    private val roomService: RoomService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun addPicture(
        @PathVariable roomId: Int,
        @RequestPart("file") file: FilePart,
        @RequestParam(required = false) caption: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "0") displayOrder: Int,
    ): ResponseEntity<RoomPictureResponse> {
        val currentUser = requireAuthenticated()
        roomService.requireOwnedByHost(roomId, currentUser.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(roomPictureService.addPicture(roomId, file, caption, isPrimary, displayOrder))
    }
}
