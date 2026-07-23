package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.util.FakeFilePart
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.webClientException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

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
        thumbnailUrl = "http://localhost:8080/uploads/rooms/$roomId/photo.jpg",
        caption = null,
        isPrimary = isPrimary,
        displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = FakeFilePart("file", name, ByteArray(8) { 0 })

    // ---- addPicture ----

    @Test
    fun addPictureRejectsNegativeDisplayOrder() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { service.addPicture(20, imageFile(), null, false, displayOrder = -1) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() =
        runTest {
            val file = imageFile()
            Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, null, isPrimary = true, displayOrder = 0))
                .thenThrow(webClientException(400, """{"message":"a primary picture already exists for this room"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.addPicture(20, file, null, isPrimary = true, displayOrder = 0) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("a primary picture already exists for this room", ex.reason)
        }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() =
        runTest {
            val file = imageFile()
            Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, "caption", false, 0)).thenReturn(mediaResponse())

            val result = service.addPicture(20, file, "caption", false, 0)

            assertEquals(1, result.id)
            assertEquals(20, result.roomId)
        }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() =
        runTest {
            Mockito.`when`(mediaFeignClient.update("ROOM", 20, 1, UpdateMediaRequest("New caption", false, 2)))
                .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

            val result = service.updatePictureMetadata(20, 1, "New caption", false, 2)

            assertEquals("New caption", result.caption)
            assertEquals(2, result.displayOrder)
        }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(mediaFeignClient.update("ROOM", 20, 99, UpdateMediaRequest(null, false, 0)))
                .thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.updatePictureMetadata(20, 99, null, false, 0) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(mediaFeignClient.delete("ROOM", 20, 99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.deletePicture(20, 99) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun deletePictureInvokesFeignClient() =
        runTest {
            service.deletePicture(20, 1)

            Mockito.verify(mediaFeignClient).delete("ROOM", 20, 1)
        }
}
