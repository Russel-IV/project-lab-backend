package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): Language CRUD now lives in identity-service,
 * reached via languageFeignClient.
 */
@Service
class LanguageService(private val languageFeignClient: LanguageFeignClient) {

    fun getAllLanguages(): List<Language> = languageFeignClient.list()

    fun createLanguage(request: LanguageRequest): Language =
        try {
            languageFeignClient.create(LanguageUpsertRequest(languageName = request.languageName))
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }

    fun updateLanguage(id: Int, request: LanguageRequest): Language =
        try {
            languageFeignClient.update(id, LanguageUpsertRequest(languageName = request.languageName))
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "languageName must not be blank")
        }

    fun deleteLanguage(id: Int) {
        try {
            languageFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "language not found")
        }
    }
}
