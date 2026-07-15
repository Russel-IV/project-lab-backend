package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.RoomRepository
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
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.Optional

class RoomPictureServiceTest {
    private val mediaFeignClient = Mockito.mock(MediaFeignClient::class.java)
    private val roomRepository = Mockito.mock(RoomRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)

    private val service = RoomPictureService(mediaFeignClient, roomRepository, stayRepository)

    private fun mediaResponse(
        id: Int = 1,
        roomId: Int = 20,
        isPrimary: Boolean = false,
    ) = MediaResponse(
        id = id, ownerType = "ROOM", ownerId = roomId,
        url = "http://localhost:8080/uploads/rooms/$roomId/photo.jpg",
        caption = null, isPrimary = isPrimary, displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    private fun sampleRoom(roomId: Int = 20, stayId: Int = 10) = Room(
        id = roomId, stayId = stayId, name = "Deluxe Suite",
        price = BigDecimal("150.00"), sleeps = 2, bedroomAmount = 1, bathrooms = BigDecimal("1.0"),
    )

    private fun sampleStay(stayId: Int = 10, hostId: Int = 1) = Stay(
        id = stayId, name = "Test Stay", propertyType = PropertyType.HOME,
        hostId = hostId,
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    private fun stubRoomAndStay(roomId: Int = 20, stayId: Int = 10, hostId: Int = 1) {
        Mockito.`when`(roomRepository.findById(roomId)).thenReturn(Optional.of(sampleRoom(roomId, stayId)))
        Mockito.`when`(stayRepository.findById(stayId)).thenReturn(Optional.of(sampleStay(stayId, hostId)))
    }

    private fun feignBadRequest(body: String) = FeignException.BadRequest(
        "bad request", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    // ---- addPicture ----

    @Test
    fun addPictureRejectsRoomNotFound() {
        Mockito.`when`(roomRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(99, imageFile(), null, false, 0, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun addPictureRejectsForbiddenWhenNotHost() {
        stubRoomAndStay(hostId = 1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, imageFile(), null, false, 0, requestingUserId = 2)
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        stubRoomAndStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, imageFile(), null, false, displayOrder = -1, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureMapsFeignBadRequestToResponseStatusException() {
        stubRoomAndStay()
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, null, isPrimary = true, displayOrder = 0))
            .thenThrow(feignBadRequest("""{"message":"a primary picture already exists for this room"}"""))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, file, null, isPrimary = true, displayOrder = 0, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("a primary picture already exists for this room", ex.reason)
    }

    @Test
    fun addPictureReturnsMappedResponseOnSuccess() {
        stubRoomAndStay()
        val file = imageFile()
        Mockito.`when`(mediaFeignClient.upload("ROOM", 20, file, "caption", false, 0)).thenReturn(mediaResponse())

        val result = service.addPicture(20, file, "caption", false, 0, 1)

        assertEquals(1, result.id)
        assertEquals(20, result.roomId)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        stubRoomAndStay()
        Mockito.`when`(mediaFeignClient.update("ROOM", 20, 1, UpdateMediaRequest("New caption", false, 2)))
            .thenReturn(mediaResponse(id = 1).copy(caption = "New caption", displayOrder = 2))

        val result = service.updatePictureMetadata(20, 1, "New caption", false, 2, 1)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        stubRoomAndStay()
        Mockito.`when`(mediaFeignClient.update("ROOM", 20, 99, UpdateMediaRequest(null, false, 0)))
            .thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(20, 99, null, false, 0, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updatePictureMetadataRejectsForbiddenWhenNotHost() {
        stubRoomAndStay(hostId = 1)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(20, 1, null, false, 0, requestingUserId = 2)
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        stubRoomAndStay()
        Mockito.`when`(mediaFeignClient.delete("ROOM", 20, 99)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deletePicture(20, 99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesFeignClient() {
        stubRoomAndStay()

        service.deletePicture(20, 1, 1)

        Mockito.verify(mediaFeignClient).delete("ROOM", 20, 1)
    }
}
