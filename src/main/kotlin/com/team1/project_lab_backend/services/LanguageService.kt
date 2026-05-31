package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.LanguageRequest
import com.team1.project_lab_backend.dto.LanguageResponse
import com.team1.project_lab_backend.models.Language
import com.team1.project_lab_backend.repositories.LanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class LanguageService(
    private val languageRepository: LanguageRepository
) {
    @Transactional(readOnly = true)
    fun getAllLanguages(): List<LanguageResponse> =
        languageRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createLanguage(request: LanguageRequest): LanguageResponse {
        if (request.languageName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }
        val language = Language(languageName = request.languageName)
        return languageRepository.save(language).toResponse()
    }

    @Transactional
    fun updateLanguage(id: Int, request: LanguageRequest): LanguageResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.languageName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }
        if (!languageRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        }
        val language = Language(id = id, languageName = request.languageName)
        return languageRepository.save(language).toResponse()
    }

    @Transactional
    fun deleteLanguage(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!languageRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        }
        languageRepository.deleteById(id)
    }
}

private fun Language.toResponse(): LanguageResponse =
    LanguageResponse(id = id, languageName = languageName)
