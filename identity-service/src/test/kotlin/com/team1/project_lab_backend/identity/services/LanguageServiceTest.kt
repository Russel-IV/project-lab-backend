package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.repositories.LanguageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LanguageServiceTest {
    private val languageRepository = Mockito.mock(LanguageRepository::class.java)
    private val service = LanguageService(languageRepository)

    @Test
    fun createLanguageRejectsBlankName() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.createLanguage(LanguageRequest(languageName = "  "))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createLanguageSaves() {
        Mockito.`when`(languageRepository.save(Mockito.any(Language::class.java))).thenAnswer { it.arguments[0] }
        val result = service.createLanguage(LanguageRequest(languageName = "English"))
        assertEquals("English", result.languageName)
    }

    @Test
    fun updateLanguageRejectsUnknown() {
        Mockito.`when`(languageRepository.existsById(99)).thenReturn(false)
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateLanguage(99, LanguageRequest(languageName = "French"))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteLanguageRejectsUnknown() {
        Mockito.`when`(languageRepository.existsById(99)).thenReturn(false)
        val ex = assertThrows(ResponseStatusException::class.java) { service.deleteLanguage(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }
}
