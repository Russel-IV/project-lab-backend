package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.inventory.services.StayFeignClient
import com.team1.project_lab_backend.media.dto.StayPictureResponse
import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.util.feignErrorMessage
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requirePositive
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): picture storage/validation and the
 * one-primary-per-owner invariant now live in media-service, reached via
 * mediaFeignClient. What's still here is the ownership check media-service can't do
 * itself — it has no Stay data — now a Feign call to inventory-service (docs/adr/0011,
 * Phase 5) rather than a local repository lookup.
 */
@Service
class StayPictureService(
    private val mediaFeignClient: MediaFeignClient,
    private val stayFeignClient: StayFeignClient,
) {
    fun getPicturesForStay(stayId: Int): List<StayPictureResponse> {
        stayId.requirePositive("stayId")
        return mediaFeignClient.listForOwner("STAY", stayId).map { it.toStayPictureResponse() }
    }

    fun getPicturesForStayAsEntities(stayId: Int): List<StayPicture> {
        stayId.requirePositive("stayId")
        return mediaFeignClient.listForOwner("STAY", stayId).map { it.toStayPicture() }
    }

    fun getPicturesForStays(stayIds: List<Int>): Map<Int, List<StayPicture>> =
        mediaFeignClient.listForOwners("STAY", stayIds).map { it.toStayPicture() }.groupBy { it.stayId }

    fun addPicture(
        stayId: Int,
        file: MultipartFile,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): StayPictureResponse {
        stayId.requirePositive("stayId")
        requireOwnedByStayHost(stayId, requestingUserId)
        displayOrder.requireNonNegative("displayOrder")
        return try {
            mediaFeignClient.upload("STAY", stayId, file, caption, isPrimary, displayOrder).toStayPictureResponse()
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid picture")
        }
    }

    fun updatePictureMetadata(
        stayId: Int,
        id: Int,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
        requestingUserId: Int,
    ): StayPicture {
        stayId.requirePositive("stayId")
        id.requirePositive()
        displayOrder.requireNonNegative("displayOrder")
        requireOwnedByStayHost(stayId, requestingUserId)
        return try {
            mediaFeignClient.update("STAY", stayId, id, UpdateMediaRequest(caption, isPrimary, displayOrder)).toStayPicture()
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid picture")
        }
    }

    fun deletePicture(
        stayId: Int,
        id: Int,
        requestingUserId: Int,
    ) {
        stayId.requirePositive("stayId")
        id.requirePositive()
        requireOwnedByStayHost(stayId, requestingUserId)
        try {
            mediaFeignClient.delete("STAY", stayId, id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        }
    }

    private fun requireOwnedByStayHost(
        stayId: Int,
        requestingUserId: Int,
    ) {
        val stay =
            try {
                stayFeignClient.get(stayId)
            } catch (e: FeignException.NotFound) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
            }
        if (stay.hostId != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
    }

    private fun MediaResponse.toStayPicture() =
        StayPicture(id = id, stayId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)

    private fun MediaResponse.toStayPictureResponse() =
        StayPictureResponse(id = id, stayId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)
}
