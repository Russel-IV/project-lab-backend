package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.StayConnection
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayPriceStats
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/**
 * Orchestration shim (docs/adr/0005): Stay CRUD, search, and the amenity/room-price
 * search predicates now live in inventory-service, reached via stayFeignClient.
 * Ownership (hostId == requestingUserId) is now checked inside inventory-service
 * itself (it owns Stay), so this shim just forwards requestingUserId and translates
 * the resulting downstream error, same pattern as every other Phase 2-4 shim.
 */
@Service
class StayService(private val stayFeignClient: StayFeignClient) {
    suspend fun searchStays(
        filter: StayFilter,
        page: Int = 0,
        size: Int = 20,
    ): StayConnection =
        try {
            stayFeignClient.search(filter, page, size)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid search filter")
        }

    suspend fun getPriceStats(
        filter: StayFilter,
        bins: Int,
    ): StayPriceStats =
        try {
            stayFeignClient.priceStats(filter, bins)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid price stats request")
        }

    suspend fun getStayById(id: Int): Stay =
        try {
            stayFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }

    suspend fun getStayByPublicId(publicId: UUID): Stay =
        try {
            stayFeignClient.getByPublicId(publicId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }

    /**
     * Cross-domain ownership check for callers outside inventory's own CRUD (e.g.
     * inventory.resolvers.StayPictureResolver) that need to authorize against a Stay's
     * host before delegating to another domain. Lives here rather than in the caller
     * because inventory owns Stay/host data — see docs/adr/0002, and the
     * ModularityTests.kt kdoc for why this used to live in media.services instead.
     */
    suspend fun requireOwnedByHost(
        id: Int,
        requestingUserId: Int,
    ): Stay {
        val stay = getStayById(id)
        if (stay.hostId != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        return stay
    }

    suspend fun createStay(
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        try {
            stayFeignClient.create(request, requestingUserId)
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid stay")
        }

    suspend fun updateStay(
        id: Int,
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        try {
            stayFeignClient.update(id, request, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid stay")
        }

    suspend fun deleteStay(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            stayFeignClient.delete(id, requestingUserId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: WebClientResponseException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
