package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.HostRequest
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.Language
import com.team1.project_lab_backend.repositories.HostRepository
import com.team1.project_lab_backend.repositories.LanguageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

class HostServiceTest {

    private val hostRepository = Mockito.mock(HostRepository::class.java)
    private val languageRepository = Mockito.mock(LanguageRepository::class.java)

    private val hostService = HostService(hostRepository, languageRepository)

    private fun baseRequest(id: Int? = 1) = HostRequest(
        id = id,
        communicationRating = BigDecimal("80.0"),
        checkinProcessRating = BigDecimal("90.0"),
        cancellationRate = BigDecimal("5.0"),
        languageIds = emptySet(),
    )

    // ---- createHost ----

    @Test
    fun createHostReturnsPersistedHost() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        val saved = Host(id = 1, communicationRating = BigDecimal("80.0"))
        Mockito.`when`(hostRepository.save(Mockito.any(Host::class.java))).thenReturn(saved)

        val result = hostService.createHost(baseRequest(id = 1))

        assertEquals(1, result.id)
    }

    @Test
    fun createHostRequiresId() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.createHost(baseRequest(id = null))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostRejectsConflictWhenAlreadyExists() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.createHost(baseRequest(id = 1))
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun createHostRejectsRatingAbove100() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.createHost(baseRequest(id = 1).copy(communicationRating = BigDecimal("100.1")))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostRejectsNegativeRating() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.createHost(baseRequest(id = 1).copy(cancellationRate = BigDecimal("-0.1")))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostWithLanguagesLooksUpLanguageEntities() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        val lang = Language(id = 5, languageName = "English")
        Mockito.`when`(languageRepository.findAllById(setOf(5))).thenReturn(listOf(lang))
        val saved = Host(id = 1, languages = mutableSetOf(lang))
        Mockito.`when`(hostRepository.save(Mockito.any(Host::class.java))).thenReturn(saved)

        val result = hostService.createHost(baseRequest(id = 1).copy(languageIds = setOf(5)))

        assertEquals(1, result.languages.size)
    }

    @Test
    fun createHostRejectsUnknownLanguageIds() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        Mockito.`when`(languageRepository.findAllById(setOf(99))).thenReturn(emptyList())

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.createHost(baseRequest(id = 1).copy(languageIds = setOf(99)))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updateHost ----

    @Test
    fun updateHostReturnsUpdatedHost() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(true)
        val saved = Host(id = 1, communicationRating = BigDecimal("70.0"))
        Mockito.`when`(hostRepository.save(Mockito.any(Host::class.java))).thenReturn(saved)

        val result = hostService.updateHost(1, baseRequest(id = null))

        assertEquals(1, result.id)
    }

    @Test
    fun updateHostRejectsIdMismatch() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.updateHost(1, baseRequest(id = 2))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateHostReturnsNotFoundWhenMissing() {
        Mockito.`when`(hostRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.updateHost(99, baseRequest(id = null))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deleteHost ----

    @Test
    fun deleteHostReturnsNotFoundWhenMissing() {
        Mockito.`when`(hostRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            hostService.deleteHost(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteHostInvokesRepository() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(true)

        hostService.deleteHost(1)

        Mockito.verify(hostRepository).deleteById(1)
    }
}
