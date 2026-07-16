package com.team1.project_lab_backend.media.services

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

/**
 * Ownership checks (stay not found / non-owner) moved to
 * inventory.services.StayServiceTest — StayPictureService no longer has a
 * StayFeignClient dependency to check ownership with; see ModularityTests.kt.
 */
class StayPictureServiceTest {
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)

    private val service = StayPictureService(mediaFeignClient)

    private fun mediaResponse(
        id: Int = 1,
        stayId: Int = 10,
        isPrimary: Boolean = false,
    ) = MediaResponse(
        id = id,
        ownerType = "STAY",
        ownerId = stayId,
        url = "http://localhost:8080/uploads/stays/$stayId/photo.jpg",
        caption = null,
        isPrimary = isPrimary,
        displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    private fun feignBadRequest(body: String) =
        FeignException.BadRequest(
            "bad request",
            Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
            body.toByteArray(StandardCharsets.UTF_8),
            emptyMap(),
        )

    // ---- addPicture ----

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, imageFile(), null, false, displayOrder = -1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() {
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, null, false, 0))
            .thenThrow(feignBadRequest("""{"message":"a primary picture already exists for this stay"}"""))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, file, null, false, 0)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("a primary picture already exists for this stay", ex.reason)
    }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() {
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, "caption", false, 0)).thenReturn(mediaResponse())

        val result = service.addPicture(10, file, "caption", false, 0)

        assertEquals(1, result.id)
        assertEquals(10, result.stayId)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        Mockito.`when`(mediaFeignClient.update("STAY", 10, 1, UpdateMediaRequest("New caption", false, 2)))
            .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

        val result = service.updatePictureMetadata(10, 1, "New caption", false, 2)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaFeignClient.update("STAY", 10, 99, UpdateMediaRequest(null, false, 0)))
            .thenThrow(FeignException.NotFound::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updatePictureMetadata(10, 99, null, false, 0)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaFeignClient.delete("STAY", 10, 99)).thenThrow(FeignException.NotFound::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.deletePicture(10, 99)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesFeignClient() {
        service.deletePicture(10, 1)

        Mockito.verify(mediaFeignClient).delete("STAY", 10, 1)
    }
}
