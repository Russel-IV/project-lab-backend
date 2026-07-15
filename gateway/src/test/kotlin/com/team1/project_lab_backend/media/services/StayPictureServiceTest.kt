package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.util.Optional

class StayPictureServiceTest {
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)

    private val service = StayPictureService(mediaFeignClient, stayRepository)

    private fun mediaResponse(
        id: Int = 1,
        stayId: Int = 10,
        isPrimary: Boolean = false,
    ) = MediaResponse(
        id = id, ownerType = "STAY", ownerId = stayId,
        url = "http://localhost:8080/uploads/stays/$stayId/photo.jpg",
        caption = null, isPrimary = isPrimary, displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    private fun sampleStay(stayId: Int = 10, hostId: Int = 1) = Stay(
        id = stayId, name = "Test Stay", propertyType = PropertyType.HOME,
        host = Host(id = hostId),
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    private fun stubStay(stayId: Int = 10, hostId: Int = 1) {
        Mockito.`when`(stayRepository.findById(stayId)).thenReturn(Optional.of(sampleStay(stayId, hostId)))
    }

    private fun feignBadRequest(body: String) = FeignException.BadRequest(
        "bad request", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    // ---- addPicture ----

    @Test
    fun addPictureRejectsStayNotFound() {
        Mockito.`when`(stayRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(99, imageFile(), null, false, 0, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNonOwner() {
        stubStay(hostId = 1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, imageFile(), null, false, 0, 2)
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        stubStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, imageFile(), null, false, displayOrder = -1, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() {
        stubStay()
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, null, false, 0))
            .thenThrow(feignBadRequest("""{"message":"a primary picture already exists for this stay"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, file, null, false, 0, 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("a primary picture already exists for this stay", ex.reason)
    }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() {
        stubStay()
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, "caption", false, 0)).thenReturn(mediaResponse())

        val result = service.addPicture(10, file, "caption", false, 0, 1)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        stubStay()
        Mockito.`when`(mediaFeignClient.update("STAY", 10, 1, UpdateMediaRequest("New caption", false, 2)))
            .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

        val result = service.updatePictureMetadata(10, 1, "New caption", false, 2, 1)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        stubStay()
        Mockito.`when`(mediaFeignClient.update("STAY", 10, 99, UpdateMediaRequest(null, false, 0)))
            .thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(10, 99, null, false, 0, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updatePictureMetadataRejectsNonOwner() {
        stubStay(hostId = 1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(10, 1, null, false, 0, 2)
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        stubStay()
        Mockito.`when`(mediaFeignClient.delete("STAY", 10, 99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deletePicture(10, 99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesFeignClient() {
        stubStay()

        service.deletePicture(10, 1, 1)

        Mockito.verify(mediaFeignClient).delete("STAY", 10, 1)
    }
}
