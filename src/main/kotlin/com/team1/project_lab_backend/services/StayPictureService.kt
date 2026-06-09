package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayPictureRequest
import com.team1.project_lab_backend.dto.StayPictureResponse
import com.team1.project_lab_backend.models.StayPicture
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StayPictureService(
    private val stayPictureRepository: StayPictureRepository,
    private val stayRepository: StayRepository
) {
    @Transactional(readOnly = true)
    fun getPicturesForStay(stayId: Int): List<StayPictureResponse> {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        return stayPictureRepository.findByStayId(stayId).map { it.toResponse() }
    }

    @Transactional
    fun addPicture(stayId: Int, request: StayPictureRequest): StayPictureResponse {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (!stayRepository.existsById(stayId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
        if (request.url.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "url must not be blank")
        }
        if (request.displayOrder < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        }
        val picture = StayPicture(
            id = 0,
            stayId = stayId,
            url = request.url,
            caption = request.caption,
            isPrimary = request.isPrimary,
            displayOrder = request.displayOrder
        )
        return stayPictureRepository.save(picture).toResponse()
    }

    @Transactional
    fun updatePicture(stayId: Int, id: Int, request: StayPictureRequest): StayPictureResponse {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        if (request.url.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "url must not be blank")
        }
        if (request.displayOrder < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayOrder must be >= 0")
        }
        val picture = StayPicture(
            id = id,
            stayId = stayId,
            url = request.url,
            caption = request.caption,
            isPrimary = request.isPrimary,
            displayOrder = request.displayOrder
        )
        return stayPictureRepository.save(picture).toResponse()
    }

    @Transactional
    fun deletePicture(stayId: Int, id: Int) {
        if (stayId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stayId must be positive")
        }
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        stayPictureRepository.findByStayIdAndId(stayId, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        stayPictureRepository.deleteById(id)
    }
}

private fun StayPicture.toResponse(): StayPictureResponse =
    StayPictureResponse(
        id = id,
        stayId = stayId,
        url = url,
        caption = caption,
        isPrimary = isPrimary,
        displayOrder = displayOrder
    )
