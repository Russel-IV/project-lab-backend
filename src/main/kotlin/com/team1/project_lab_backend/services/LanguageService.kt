package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.LanguageRequest
import com.team1.project_lab_backend.dto.LanguageResponse
import com.team1.project_lab_backend.models.Language
import com.team1.project_lab_backend.repositories.LanguageRepository
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguageService(
    private val languageRepository: LanguageRepository
) {
    @Transactional(readOnly = true)
    fun getAllLanguages(): List<LanguageResponse> =
        languageRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createLanguage(request: LanguageRequest): LanguageResponse {
        request.languageName.requireNotBlank("languageName")
        val language = Language(languageName = request.languageName)
        return languageRepository.save(language).toResponse()
    }

    @Transactional
    fun updateLanguage(id: Int, request: LanguageRequest): LanguageResponse {
        id.requirePositive()
        request.languageName.requireNotBlank("languageName")
        languageRepository.requireExistsById(id, "language not found")
        val language = Language(id = id, languageName = request.languageName)
        return languageRepository.save(language).toResponse()
    }

    @Transactional
    fun deleteLanguage(id: Int) {
        id.requirePositive()
        languageRepository.requireExistsById(id, "language not found")
        languageRepository.deleteById(id)
    }
}

private fun Language.toResponse(): LanguageResponse =
    LanguageResponse(id = id, languageName = languageName)
