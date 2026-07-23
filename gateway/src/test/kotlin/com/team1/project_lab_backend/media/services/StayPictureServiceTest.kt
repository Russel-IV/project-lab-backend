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
        thumbnailUrl = "http://localhost:8080/uploads/stays/$stayId/photo.jpg",
        url1024 = null,
        url512 = null,
        caption = null,
        isPrimary = isPrimary,
        displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = FakeFilePart("file", name, ByteArray(8) { 0 })

    // ---- addPicture ----

    @Test
    fun addPictureRejectsNegativeDisplayOrder() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { service.addPicture(10, imageFile(), null, false, displayOrder = -1) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() =
        runTest {
            val file = imageFile()
            Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, null, false, 0))
                .thenThrow(webClientException(400, """{"message":"a primary picture already exists for this stay"}"""))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.addPicture(10, file, null, false, 0) }
            assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
            assertEquals("a primary picture already exists for this stay", ex.reason)
        }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() =
        runTest {
            val file = imageFile()
            Mockito.`when`(mediaFeignClient.upload("STAY", 10, file, "caption", false, 0)).thenReturn(mediaResponse())

            val result = service.addPicture(10, file, "caption", false, 0)

            assertEquals(1, result.id)
            assertEquals(10, result.stayId)
        }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() =
        runTest {
            Mockito.`when`(mediaFeignClient.update("STAY", 10, 1, UpdateMediaRequest("New caption", false, 2)))
                .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

            val result = service.updatePictureMetadata(10, 1, "New caption", false, 2)

            assertEquals("New caption", result.caption)
            assertEquals(2, result.displayOrder)
        }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(mediaFeignClient.update("STAY", 10, 99, UpdateMediaRequest(null, false, 0)))
                .thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.updatePictureMetadata(10, 99, null, false, 0) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() =
        runTest {
            Mockito.`when`(mediaFeignClient.delete("STAY", 10, 99)).thenThrow(webClientException(404))

            val ex = assertThrowsSuspend<ResponseStatusException> { service.deletePicture(10, 99) }
            assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        }

    @Test
    fun deletePictureInvokesFeignClient() =
        runTest {
            service.deletePicture(10, 1)

            Mockito.verify(mediaFeignClient).delete("STAY", 10, 1)
        }
}
