package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

class HostServiceTest {
    private val hostFeignClient = Mockito.mock(HostFeignClient::class.java)
    private val hostService = HostService(hostFeignClient)

    private fun baseRequest(id: Int? = 1) =
        HostRequest(
            id = id,
            communicationRating = BigDecimal("80.0"),
            checkinProcessRating = BigDecimal("90.0"),
            cancellationRate = BigDecimal("5.0"),
            languageIds = emptySet(),
        )

    private fun upsertRequest(id: Int? = 1) =
        HostUpsertRequest(
            id = id,
            communicationRating = BigDecimal("80.0"),
            checkinProcessRating = BigDecimal("90.0"),
            cancellationRate = BigDecimal("5.0"),
            languageIds = emptySet(),
        )

    // ---- createHost ----

    @Test
    fun createHostReturnsPersistedHost() =
        runTest {
            Mockito.`when`(hostFeignClient.create(upsertRequest(1)))
                .thenReturn(Host(id = 1, communicationRating = BigDecimal("80.0"), checkinProcessRating = null, cancellationRate = null))

            val result = hostService.createHost(baseRequest(id = 1))

            assertEquals(1, result.id)
        }

    @Test
    fun createHostRejectsConflictWhenAlreadyExists() =
        runTest {
            Mockito.`when`(hostFeignClient.create(upsertRequest(1))).thenThrow(webClientException(409))

            val ex = assertThrowsSuspend<ResponseStatusException> { hostService.createHost(baseRequest(id = 1)) }
            assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        }

    @Test
    fun createHostMapsFeignBadRequestWithMessage() =
        runTest {
            Mockito.`when`(hostFeignClient.create(upsertRequest(1)))
                .thenThrow(webClientException(400, """{"message":"communicationRating must be between 0 and 100"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { hostService.createHost(baseRequest(id = 1)) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("communicationRating must be between 0 and 100", ex.reason)
        }

    // ---- updateHost ----

    @Test
    fun updateHostReturnsUpdatedHost() =
        runTest {
            Mockito.`when`(hostFeignClient.update(1, upsertRequest(null)))
                .thenReturn(Host(id = 1, communicationRating = BigDecimal("70.0"), checkinProcessRating = null, cancellationRate = null))

            val result = hostService.updateHost(1, baseRequest(id = null))

            assertEquals(1, result.id)
        }

    @Test
    fun updateHostReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(hostFeignClient.update(99, upsertRequest(null))).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { hostService.updateHost(99, baseRequest(id = null)) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- deleteHost ----

    @Test
    fun deleteHostReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(hostFeignClient.delete(99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { hostService.deleteHost(99) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun deleteHostInvokesFeignClient() =
        runTest {
            hostService.deleteHost(1)

            Mockito.verify(hostFeignClient).delete(1)
        }
}
