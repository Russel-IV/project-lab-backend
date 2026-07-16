package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Stay CRUD, search, and the amenity/room-price
 * search predicates now live in inventory-service, reached via stayFeignClient.
 * Ownership (hostId == requestingUserId) is now checked inside inventory-service
 * itself (it owns Stay), so this shim just forwards requestingUserId and translates
 * the resulting Feign error, same pattern as every other Phase 2-4 shim.
 */
@Service
class StayService(private val stayFeignClient: StayFeignClient) {
    fun searchStays(
        filter: StayFilter,
        page: Int = 0,
        size: Int = 20,
    ): List<Stay> =
        try {
            stayFeignClient.search(filter, page, size)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid search filter")
        }

    fun getStayById(id: Int): Stay =
        try {
            stayFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }

    /**
     * Cross-domain ownership check for callers outside inventory's own CRUD (e.g.
     * inventory.resolvers.StayPictureResolver) that need to authorize against a Stay's
     * host before delegating to another domain. Lives here rather than in the caller
     * because inventory owns Stay/host data — see docs/adr/0002, and the
     * ModularityTests.kt kdoc for why this used to live in media.services instead.
     */
    fun requireOwnedByHost(
        id: Int,
        requestingUserId: Int,
    ): Stay {
        val stay = getStayById(id)
        if (stay.hostId != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        return stay
    }

    fun createStay(
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        try {
            stayFeignClient.create(request, requestingUserId)
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid stay")
        }

    fun updateStay(
        id: Int,
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        try {
            stayFeignClient.update(id, request, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid stay")
        }

    fun deleteStay(
        id: Int,
        requestingUserId: Int,
    ) {
        try {
            stayFeignClient.delete(id, requestingUserId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        }
    }
}
