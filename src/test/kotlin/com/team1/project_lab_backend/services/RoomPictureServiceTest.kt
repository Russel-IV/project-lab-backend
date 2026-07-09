package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.PropertyType
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.models.RoomPicture
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.repositories.RoomPictureRepository
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

class RoomPictureServiceTest {

    private val roomPictureRepository = Mockito.mock(RoomPictureRepository::class.java)
    private val roomRepository = Mockito.mock(RoomRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)
    private val storageService = Mockito.mock(StorageService::class.java)

    private val service = RoomPictureService(roomPictureRepository, roomRepository, stayRepository, storageService)

    private fun picture(id: Int = 1, roomId: Int = 20, isPrimary: Boolean = false) =
        RoomPicture(id = id, roomId = roomId, url = "rooms/$roomId/photo.jpg",
            caption = null, isPrimary = isPrimary, displayOrder = 0)

    private fun imageFile(name: String = "photo.jpg") =
        MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    private fun sampleRoom(roomId: Int = 20, stayId: Int = 10) = Room(
        id = roomId, stayId = stayId, name = "Deluxe Suite",
        price = BigDecimal("150.00"), sleeps = 2, bedroomAmount = 1,
        bathrooms = BigDecimal("1.0"),
    )

    private fun sampleStay(stayId: Int = 10, hostId: Int = 1) = Stay(
        id = stayId,
        name = "Test Stay",
        propertyType = PropertyType.HOME,
        host = Host(id = hostId),
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    private fun stubRoomAndStay(roomId: Int = 20, stayId: Int = 10, hostId: Int = 1) {
        Mockito.`when`(roomRepository.findById(roomId)).thenReturn(Optional.of(sampleRoom(roomId, stayId)))
        Mockito.`when`(stayRepository.findById(stayId)).thenReturn(Optional.of(sampleStay(stayId, hostId)))
    }

    // ---- addPicture ----

    @Test
    fun addPictureRejectsEmptyFile() {
        stubRoomAndStay()
        val emptyFile = MockMultipartFile("file", "photo.jpg", "image/jpeg", ByteArray(0))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, emptyFile, null, false, 0, 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNonImageContentType() {
        stubRoomAndStay()
        val pdf = MockMultipartFile("file", "doc.pdf", "application/pdf", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, pdf, null, false, 0, 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

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
    fun addPictureRejectsDuplicatePrimary() {
        stubRoomAndStay()
        Mockito.`when`(roomPictureRepository.existsByRoomIdAndIsPrimary(20, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, imageFile(), null, isPrimary = true, displayOrder = 0, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsDisallowedExtension() {
        stubRoomAndStay()
        val html = MockMultipartFile("file", "exploit.html", "image/jpeg", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, html, null, false, 0, 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsPhpExtension() {
        stubRoomAndStay()
        val php = MockMultipartFile("file", "shell.php", "image/jpeg", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, php, null, false, 0, 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        stubRoomAndStay()

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(20, imageFile(), null, false, displayOrder = -1, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        stubRoomAndStay()
        val existing = picture(id = 1, roomId = 20, isPrimary = false)
        Mockito.`when`(roomPictureRepository.findByRoomIdAndId(20, 1)).thenReturn(existing)
        val saved = existing.copy(caption = "New caption", displayOrder = 2)
        Mockito.`when`(roomPictureRepository.save(Mockito.any(RoomPicture::class.java))).thenReturn(saved)

        val result = service.updatePictureMetadata(20, 1, "New caption", false, 2, 1)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        stubRoomAndStay()
        Mockito.`when`(roomPictureRepository.findByRoomIdAndId(20, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(20, 99, null, false, 0, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updatePictureMetadataRejectsDuplicatePrimaryWhenCurrentIsNot() {
        stubRoomAndStay()
        val existing = picture(id = 1, roomId = 20, isPrimary = false)
        Mockito.`when`(roomPictureRepository.findByRoomIdAndId(20, 1)).thenReturn(existing)
        Mockito.`when`(roomPictureRepository.existsByRoomIdAndIsPrimary(20, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(20, 1, null, isPrimary = true, displayOrder = 0, requestingUserId = 1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        stubRoomAndStay()
        Mockito.`when`(roomPictureRepository.findByRoomIdAndId(20, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deletePicture(20, 99, 1)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesRepository() {
        stubRoomAndStay()
        val existing = picture(id = 1, roomId = 20)
        Mockito.`when`(roomPictureRepository.findByRoomIdAndId(20, 1)).thenReturn(existing)

        service.deletePicture(20, 1, 1)

        Mockito.verify(roomPictureRepository).deleteById(1)
    }
}

private fun RoomPicture.copy(caption: String? = this.caption, displayOrder: Int = this.displayOrder) =
    RoomPicture(id = id, roomId = roomId, url = url, caption = caption,
        isPrimary = isPrimary, displayOrder = displayOrder)
