package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.HostRequest
import com.team1.project_lab_backend.dto.HostResponse
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.repositories.HostRepository
import com.team1.project_lab_backend.repositories.LanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

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
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return hostRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "host not found") }
    }

    @Transactional
    fun createHost(request: HostRequest): HostResponse {
        val hostId = request.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required")
        if (hostId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (hostRepository.existsById(hostId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "host already exists")
        }
        val max = java.math.BigDecimal("100.0")
        request.communicationRating?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "communicationRating must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "communicationRating must be <= 100.0")
            }
        }
        request.checkinProcessRating?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkinProcessRating must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkinProcessRating must be <= 100.0")
            }
        }
        request.cancellationRate?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cancellationRate must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cancellationRate must be <= 100.0")
            }
        }
        if (request.languageIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageIds must contain only positive ids")
        }
        val host = buildHost(hostId, request)
        return hostRepository.save(host).toResponse()
    }

    @Transactional
    fun updateHost(id: Int, request: HostRequest): HostResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.id != null && request.id != id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id mismatch")
        }
        if (!hostRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        }
        if (request.id != null) {
            if (request.id <= 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
            }
        }
        val max = java.math.BigDecimal("100.0")
        request.communicationRating?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "communicationRating must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "communicationRating must be <= 100.0")
            }
        }
        request.checkinProcessRating?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkinProcessRating must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkinProcessRating must be <= 100.0")
            }
        }
        request.cancellationRate?.let {
            if (it.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cancellationRate must be >= 0")
            }
            if (it.compareTo(max) > 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cancellationRate must be <= 100.0")
            }
        }
        if (request.languageIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageIds must contain only positive ids")
        }
        val host = buildHost(id, request)
        return hostRepository.save(host).toResponse()
    }

    @Transactional
    fun deleteHost(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!hostRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        }
        hostRepository.deleteById(id)
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
