package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.StayPictureRequest
import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.services.StayPictureService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stays/{stayId}/pictures")
class StayPictureController(
    private val stayPictureService: StayPictureService
) {
    @GetMapping
    fun getPicturesForStay(@PathVariable stayId: Int): ResponseEntity<List<StayPictureResponse>> =
        ResponseEntity.ok(stayPictureService.getPicturesForStay(stayId))

    @PostMapping
    fun addPicture(
        @PathVariable stayId: Int,
        @RequestBody request: StayPictureRequest
    ): ResponseEntity<StayPictureResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(stayPictureService.addPicture(stayId, request))

    @PutMapping("/{id}")
    fun updatePicture(
        @PathVariable stayId: Int,
        @PathVariable id: Int,
        @RequestBody request: StayPictureRequest
    ): ResponseEntity<StayPictureResponse> =
        ResponseEntity.ok(stayPictureService.updatePicture(stayId, id, request))

    @DeleteMapping("/{id}")
    fun deletePicture(
        @PathVariable stayId: Int,
        @PathVariable id: Int
    ): ResponseEntity<Unit> =
        stayPictureService.deletePicture(stayId, id).let { ResponseEntity.noContent().build() }
}
