package com.team1.project_lab_backend.media.services

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
 * mediaFeignClient. The stay-ownership check media-service can't do itself (it has no
 * Stay data) now lives in inventory.services.StayService.requireOwnedByHost, called by
 * inventory.resolvers.StayPictureResolver/inventory.controllers.StayPictureController
 * before delegating here — moved out of this class to break a Spring Modulith cycle
 * (identity -> media -> inventory -> identity); see ModularityTests.kt.
 */
@Service
class StayPictureService(
    private val mediaFeignClient: MediaFeignClient,
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
    ): StayPictureResponse {
        stayId.requirePositive("stayId")
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
    ): StayPicture {
        stayId.requirePositive("stayId")
        id.requirePositive()
        displayOrder.requireNonNegative("displayOrder")
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
    ) {
        stayId.requirePositive("stayId")
        id.requirePositive()
        try {
            mediaFeignClient.delete("STAY", stayId, id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        }
    }

    private fun MediaResponse.toStayPicture() =
        StayPicture(id = id, stayId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)

    private fun MediaResponse.toStayPictureResponse() =
        StayPictureResponse(id = id, stayId = ownerId, url = url, caption = caption, isPrimary = isPrimary, displayOrder = displayOrder)
}
