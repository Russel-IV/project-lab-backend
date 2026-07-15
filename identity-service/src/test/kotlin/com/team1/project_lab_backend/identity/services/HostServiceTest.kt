package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.repositories.HostRepository
import com.team1.project_lab_backend.identity.repositories.LanguageRepository
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
    private val service = HostService(hostRepository, languageRepository)

    @Test
    fun createHostRequiresId() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.createHost(HostRequest(id = null))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostRejectsWhenAlreadyExists() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(true)
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.createHost(HostRequest(id = 1))
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun createHostRejectsRatingOutOfRange() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.createHost(HostRequest(id = 1, communicationRating = BigDecimal("150")))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostRejectsUnknownLanguageIds() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        Mockito.`when`(languageRepository.findAllById(setOf(5, 6))).thenReturn(listOf(Language(id = 5, languageName = "English")))
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.createHost(HostRequest(id = 1, languageIds = setOf(5, 6)))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createHostSavesHost() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        Mockito.`when`(hostRepository.save(Mockito.any(Host::class.java))).thenAnswer { it.arguments[0] }
        val result = service.createHost(HostRequest(id = 1))
        assertEquals(1, result.id)
    }

    @Test
    fun updateHostRejectsIdMismatch() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateHost(1, HostRequest(id = 2))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateHostRejectsUnknownHost() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateHost(1, HostRequest())
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getHostByIdReturnsNotFoundWhenMissing() {
        Mockito.`when`(hostRepository.findById(99)).thenReturn(Optional.empty())
        val ex = assertThrows(ResponseStatusException::class.java) { service.getHostById(99) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteHostRejectsUnknownHost() {
        Mockito.`when`(hostRepository.existsById(1)).thenReturn(false)
        val ex = assertThrows(ResponseStatusException::class.java) { service.deleteHost(1) }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }
}
