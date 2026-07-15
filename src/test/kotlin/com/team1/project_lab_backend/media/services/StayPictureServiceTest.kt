package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.media.repositories.StayPictureRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class StayPictureServiceTest {
    private val stayPictureRepository = Mockito.mock(StayPictureRepository::class.java)
    private val stayRepository = Mockito.mock(StayRepository::class.java)
    private val storageService = Mockito.mock(StorageService::class.java)

    private val service = StayPictureService(stayPictureRepository, stayRepository, storageService)

    private fun picture(
        id: Int = 1,
        stayId: Int = 10,
        isPrimary: Boolean = false,
    ) = StayPicture(
        id = id,
        stayId = stayId,
        url = "stays/$stayId/photo.jpg",
        caption = null,
        isPrimary = isPrimary,
        displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    private fun sampleStay(
        stayId: Int = 10,
        hostId: Int = 1,
    ) = Stay(
        id = stayId,
        name = "Test Stay",
        propertyType = PropertyType.HOME,
        host = Host(id = hostId),
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
    )

    private fun stubStay(
        stayId: Int = 10,
        hostId: Int = 1,
    ) {
        Mockito.`when`(stayRepository.findById(stayId)).thenReturn(Optional.of(sampleStay(stayId, hostId)))
    }

    // ---- addPicture ----

    @Test
    fun addPictureRejectsEmptyFile() {
        stubStay()
        val emptyFile = MockMultipartFile("file", "photo.jpg", "image/jpeg", ByteArray(0))

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, emptyFile, null, false, 0, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNonImageContentType() {
        stubStay()
        val pdf = MockMultipartFile("file", "doc.pdf", "application/pdf", ByteArray(8) { 0 })

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, pdf, null, false, 0, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsStayNotFound() {
        Mockito.`when`(stayRepository.findById(99)).thenReturn(Optional.empty())

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(99, imageFile(), null, false, 0, 1)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun addPictureRejectsDuplicatePrimary() {
        stubStay()
        Mockito.`when`(stayPictureRepository.existsByStayIdAndIsPrimary(10, true)).thenReturn(true)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, imageFile(), null, isPrimary = true, displayOrder = 0, requestingUserId = 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsDisallowedExtension() {
        stubStay()
        val html = MockMultipartFile("file", "exploit.html", "image/jpeg", ByteArray(8) { 0 })

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, html, null, false, 0, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsPhpExtension() {
        stubStay()
        val php = MockMultipartFile("file", "shell.php", "image/jpeg", ByteArray(8) { 0 })

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, php, null, false, 0, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        stubStay()

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.addPicture(10, imageFile(), null, false, displayOrder = -1, requestingUserId = 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        stubStay()
        val existing = picture(id = 1, stayId = 10, isPrimary = false)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)
        val saved = existing.copy(caption = "New caption", displayOrder = 2)
        Mockito.`when`(stayPictureRepository.save(Mockito.any(StayPicture::class.java))).thenReturn(saved)

        val result = service.updatePictureMetadata(10, 1, "New caption", false, 2, 1)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        stubStay()
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 99)).thenReturn(null)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updatePictureMetadata(10, 99, null, false, 0, 1)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updatePictureMetadataRejectsDuplicatePrimaryWhenCurrentIsNot() {
        stubStay()
        val existing = picture(id = 1, stayId = 10, isPrimary = false)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)
        Mockito.`when`(stayPictureRepository.existsByStayIdAndIsPrimary(10, true)).thenReturn(true)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.updatePictureMetadata(10, 1, null, isPrimary = true, displayOrder = 0, requestingUserId = 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        stubStay()
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 99)).thenReturn(null)

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.deletePicture(10, 99, 1)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesRepository() {
        stubStay()
        val existing = picture(id = 1, stayId = 10)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)

        service.deletePicture(10, 1, 1)

        Mockito.verify(stayPictureRepository).deleteById(1)
    }
}

private fun StayPicture.copy(
    caption: String? = this.caption,
    displayOrder: Int = this.displayOrder,
) = StayPicture(
    id = id,
    stayId = stayId,
    url = url,
    caption = caption,
    isPrimary = isPrimary,
    displayOrder = displayOrder,
)
