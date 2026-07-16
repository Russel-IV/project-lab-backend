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
 * Ownership checks (room/stay not found / non-owner) moved to
 * inventory.services.RoomServiceTest — RoomPictureService no longer has
 * RoomFeignClient/StayFeignClient dependencies to check ownership with; see
 * ModularityTests.kt.
 */
class RoomPictureServiceTest {
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)

    private val service = RoomPictureService(mediaFeignClient)

    private fun mediaResponse(
        id: Int = 1,
        roomId: Int = 20,
        isPrimary: Boolean = false,
    ) = MediaResponse(
        id = id,
        ownerType = "ROOM",
        ownerId = roomId,
        url = "http://localhost:8080/uploads/rooms/$roomId/photo.jpg",
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
                service.addPicture(20, imageFile(), null, false, displayOrder = -1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() {
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, null, isPrimary = true, displayOrder = 0))
            .thenThrow(feignBadRequest("""{"message":"a primary picture already exists for this room"}"""))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(20, file, null, isPrimary = true, displayOrder = 0)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("a primary picture already exists for this room", ex.reason)
    }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() {
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, "caption", false, 0)).thenReturn(mediaResponse())

        val result = service.addPicture(20, file, "caption", false, 0)

        assertEquals(1, result.id)
        assertEquals(20, result.roomId)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        Mockito.`when`(mediaFeignClient.update("ROOM", 20, 1, UpdateMediaRequest("New caption", false, 2)))
            .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

        val result = service.updatePictureMetadata(20, 1, "New caption", false, 2)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaFeignClient.update("ROOM", 20, 99, UpdateMediaRequest(null, false, 0)))
            .thenThrow(FeignException.NotFound::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updatePictureMetadata(20, 99, null, false, 0)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaFeignClient.delete("ROOM", 20, 99)).thenThrow(FeignException.NotFound::class.java)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.deletePicture(20, 99)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesFeignClient() {
        service.deletePicture(20, 1)

        Mockito.verify(mediaFeignClient).delete("ROOM", 20, 1)
    }
}
