package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Host CRUD, rating validation, and language
 * lookup now live in identity-service, reached via hostFeignClient.
 */
@Service
class HostService(private val hostFeignClient: HostFeignClient) {
    suspend fun getAllHosts(): List<Host> = hostFeignClient.list(ids = null)

    suspend fun getHostById(id: Int): Host =
        try {
            hostFeignClient.get(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        }

    suspend fun createHost(request: HostRequest): Host =
        try {
            hostFeignClient.create(request.toUpsertRequest())
        } catch (e: WebClientResponseException.Conflict) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "host already exists")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid host")
        }

    suspend fun updateHost(
        id: Int,
        request: HostRequest,
    ): Host =
        try {
            hostFeignClient.update(id, request.toUpsertRequest())
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid host")
        }

    suspend fun deleteHost(id: Int) {
        try {
            hostFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
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
