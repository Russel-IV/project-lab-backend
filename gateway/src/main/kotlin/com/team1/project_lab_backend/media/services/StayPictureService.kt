package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.dto.StayPictureResponse
import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.util.webClientErrorMessage
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
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
    suspend fun getPicturesForStay(stayId: Int): List<StayPictureResponse> {
        stayId.requirePositive("stayId")
        return mediaFeignClient.listForOwner("STAY", stayId).map { it.toStayPictureResponse() }
    }

    suspend fun getPicturesForStayAsEntities(stayId: Int): List<StayPicture> {
        stayId.requirePositive("stayId")
        return mediaFeignClient.listForOwner("STAY", stayId).map { it.toStayPicture() }
    }

    suspend fun getPicturesForStays(stayIds: List<Int>): Map<Int, List<StayPicture>> =
        mediaFeignClient.listForOwners("STAY", stayIds).map { it.toStayPicture() }.groupBy { it.stayId }

    suspend fun addPicture(
        stayId: Int,
        file: FilePart,
        caption: String?,
        isPrimary: Boolean,
        displayOrder: Int,
    ): StayPictureResponse {
        stayId.requirePositive("stayId")
        displayOrder.requireNonNegative("displayOrder")
        return try {
            mediaFeignClient.upload("STAY", stayId, file, caption, isPrimary, displayOrder).toStayPictureResponse()
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid picture")
        }
    }

    suspend fun updatePictureMetadata(
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
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid picture")
        }
    }

    suspend fun deletePicture(
        stayId: Int,
        id: Int,
    ) {
        stayId.requirePositive("stayId")
        id.requirePositive()
        try {
            mediaFeignClient.delete("STAY", stayId, id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "picture not found")
        }
    }

    private fun MediaResponse.toStayPicture() =
        StayPicture(
            id = id,
            stayId = ownerId,
            url = url,
            thumbnailUrl = thumbnailUrl,
            url1024 = url1024,
            url512 = url512,
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )

    private fun MediaResponse.toStayPictureResponse() =
        StayPictureResponse(
            id = id,
            stayId = ownerId,
            url = url,
            thumbnailUrl = thumbnailUrl,
            url1024 = url1024,
            url512 = url512,
            caption = caption,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
        )
}
