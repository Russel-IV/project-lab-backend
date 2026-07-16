package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Host CRUD, rating validation, and language
 * lookup now live in identity-service, reached via hostFeignClient.
 */
@Service
class HostService(private val hostFeignClient: HostFeignClient) {
    fun getAllHosts(): List<Host> = hostFeignClient.list(ids = null)

    fun getHostById(id: Int): Host =
        try {
            hostFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        }

    fun createHost(request: HostRequest): Host =
        try {
            hostFeignClient.create(request.toUpsertRequest())
        } catch (e: FeignException.Conflict) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "host already exists")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid host")
        }

    fun updateHost(
        id: Int,
        request: HostRequest,
    ): Host =
        try {
            hostFeignClient.update(id, request.toUpsertRequest())
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid host")
        }

    fun deleteHost(id: Int) {
        try {
            hostFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        }
    }

    private fun HostRequest.toUpsertRequest() =
        HostUpsertRequest(
            id = id,
            communicationRating = communicationRating,
            checkinProcessRating = checkinProcessRating,
            cancellationRate = cancellationRate,
            languageIds = languageIds,
        )
}
