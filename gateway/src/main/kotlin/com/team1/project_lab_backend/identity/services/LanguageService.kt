package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.repositories.LanguageRepository
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguageService(
    private val languageRepository: LanguageRepository,
) {
    @Transactional(readOnly = true)
    fun getAllLanguages(): List<Language> = languageRepository.findAll()

    @Transactional
    fun createLanguage(request: LanguageRequest): Language {
        request.languageName.requireNotBlank("languageName")
        return languageRepository.save(Language(languageName = request.languageName))
    }

    @Transactional
    fun updateLanguage(id: Int, request: LanguageRequest): Language {
        id.requirePositive()
        request.languageName.requireNotBlank("languageName")
        languageRepository.requireExistsById(id, "language not found")
        return languageRepository.save(Language(id = id, languageName = request.languageName))
    }

    @Transactional
    fun deleteLanguage(id: Int) {
        id.requirePositive()
        languageRepository.requireExistsById(id, "language not found")
        languageRepository.deleteById(id)
    }
}
