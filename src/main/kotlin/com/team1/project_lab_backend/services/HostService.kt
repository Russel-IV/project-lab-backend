package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.HostRequest
import com.team1.project_lab_backend.dto.HostResponse
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.repositories.HostRepository
import com.team1.project_lab_backend.repositories.LanguageRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireAllPositive
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireInRange
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

private val RATING_MAX = BigDecimal("100.0")

@Service
class HostService(
    private val hostRepository: HostRepository,
    private val languageRepository: LanguageRepository
) {
    @Transactional(readOnly = true)
    fun getAllHosts(): List<HostResponse> =
        hostRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getHostById(id: Int): HostResponse {
        id.requirePositive()
        return hostRepository.findById(id).orNotFound("host not found").toResponse()
    }

    @Transactional
    fun createHost(request: HostRequest): HostResponse {
        val hostId = request.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required")
        hostId.requirePositive()
        if (hostRepository.existsById(hostId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "host already exists")
        }
        validateHostRequest(request)
        val host = buildHost(hostId, request)
        return hostRepository.save(host).toResponse()
    }

    @Transactional
    fun updateHost(id: Int, request: HostRequest): HostResponse {
        id.requirePositive()
        if (request.id != null && request.id != id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id mismatch")
        }
        hostRepository.requireExistsById(id, "host not found")
        validateHostRequest(request)
        val host = buildHost(id, request)
        return hostRepository.save(host).toResponse()
    }

    @Transactional
    fun deleteHost(id: Int) {
        id.requirePositive()
        hostRepository.requireExistsById(id, "host not found")
        hostRepository.deleteById(id)
    }

    private fun validateHostRequest(request: HostRequest) {
        request.communicationRating?.requireInRange(BigDecimal.ZERO, RATING_MAX, "communicationRating")
        request.checkinProcessRating?.requireInRange(BigDecimal.ZERO, RATING_MAX, "checkinProcessRating")
        request.cancellationRate?.requireInRange(BigDecimal.ZERO, RATING_MAX, "cancellationRate")
        request.languageIds.requireAllPositive("languageIds")
    }

    private fun buildHost(id: Int, request: HostRequest): Host {
        val languages = if (request.languageIds.isEmpty()) {
            mutableSetOf()
        } else {
            val found = languageRepository.findAllById(request.languageIds).toList()
            if (found.size != request.languageIds.size) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageIds contains unknown ids")
            }
            found.toMutableSet()
        }
        return Host(
            id = id,
            communicationRating = request.communicationRating,
            checkinProcessRating = request.checkinProcessRating,
            cancellationRate = request.cancellationRate,
            languages = languages
        )
    }
}

private fun Host.toResponse(): HostResponse =
    HostResponse(
        id = id,
        communicationRating = communicationRating,
        checkinProcessRating = checkinProcessRating,
        cancellationRate = cancellationRate,
        languageIds = languages.map { it.id }.toSet()
    )
