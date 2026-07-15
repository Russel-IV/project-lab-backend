package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.repositories.HostRepository
import com.team1.project_lab_backend.identity.repositories.LanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

private val RATING_MAX = BigDecimal("100.0")

@Service
class HostService(
    private val hostRepository: HostRepository,
    private val languageRepository: LanguageRepository,
) {
    @Transactional(readOnly = true)
    fun getAllHosts(): List<Host> = hostRepository.findAll()

    @Transactional(readOnly = true)
    fun getHostById(id: Int): Host =
        hostRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "host not found") }

    @Transactional(readOnly = true)
    fun getHostsByIds(ids: List<Int>): List<Host> = hostRepository.findByIdInWithLanguages(ids)

    @Transactional
    fun createHost(request: HostRequest): Host {
        val hostId = request.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required")
        if (hostId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        if (hostRepository.existsById(hostId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "host already exists")
        }
        validateHostRequest(request)
        return hostRepository.save(buildHost(hostId, request))
    }

    @Transactional
    fun updateHost(id: Int, request: HostRequest): Host {
        if (request.id != null && request.id != id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id mismatch")
        }
        if (!hostRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        validateHostRequest(request)
        return hostRepository.save(buildHost(id, request))
    }

    @Transactional
    fun deleteHost(id: Int) {
        if (!hostRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "host not found")
        hostRepository.deleteById(id)
    }

    private fun validateHostRequest(request: HostRequest) {
        request.communicationRating?.let { requireInRange(it, "communicationRating") }
        request.checkinProcessRating?.let { requireInRange(it, "checkinProcessRating") }
        request.cancellationRate?.let { requireInRange(it, "cancellationRate") }
        if (request.languageIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageIds must contain only positive ids")
        }
    }

    private fun requireInRange(value: BigDecimal, field: String) {
        if (value < BigDecimal.ZERO || value > RATING_MAX) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be between 0 and 100")
        }
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
            languages = languages,
        )
    }
}
