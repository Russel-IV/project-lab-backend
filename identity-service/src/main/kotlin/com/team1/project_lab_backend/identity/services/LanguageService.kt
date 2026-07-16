package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.repositories.LanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class LanguageService(
    private val languageRepository: LanguageRepository,
) {
    @Transactional(readOnly = true)
    fun getAllLanguages(): List<Language> = languageRepository.findAll()

    @Transactional
    fun createLanguage(request: LanguageRequest): Language {
        if (request.languageName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }
        return languageRepository.save(Language(languageName = request.languageName))
    }

    @Transactional
    fun updateLanguage(
        id: Int,
        request: LanguageRequest,
    ): Language {
        if (request.languageName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }
        if (!languageRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        return languageRepository.save(Language(id = id, languageName = request.languageName))
    }

    @Transactional
    fun deleteLanguage(id: Int) {
        if (!languageRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        languageRepository.deleteById(id)
    }
}
