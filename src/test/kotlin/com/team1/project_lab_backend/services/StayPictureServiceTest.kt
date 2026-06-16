package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.models.StayPicture
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
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

    private val uploadDir = System.getProperty("java.io.tmpdir")

    private val service = StayPictureService(stayPictureRepository, stayRepository, uploadDir)

    private fun picture(id: Int = 1, stayId: Int = 10, isPrimary: Boolean = false) =
        StayPicture(id = id, stayId = stayId, url = "/uploads/stays/$stayId/photo.jpg",
            caption = null, isPrimary = isPrimary, displayOrder = 0)

    private fun imageFile(name: String = "photo.jpg") =
        MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    // ---- addPicture ----

    @Test
    fun addPictureRejectsEmptyFile() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)
        val emptyFile = MockMultipartFile("file", "photo.jpg", "image/jpeg", ByteArray(0))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, emptyFile, null, false, 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNonImageContentType() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)
        val pdf = MockMultipartFile("file", "doc.pdf", "application/pdf", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, pdf, null, false, 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsStayNotFound() {
        Mockito.`when`(stayRepository.existsById(99)).thenReturn(false)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(99, imageFile(), null, false, 0)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun addPictureRejectsDuplicatePrimary() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)
        Mockito.`when`(stayPictureRepository.existsByStayIdAndIsPrimary(10, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, imageFile(), null, isPrimary = true, displayOrder = 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addPictureRejectsNegativeDisplayOrder() {
        Mockito.`when`(stayRepository.existsById(10)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addPicture(10, imageFile(), null, false, displayOrder = -1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- updatePictureMetadata ----

    @Test
    fun updatePictureMetadataReturnsUpdatedPicture() {
        val existing = picture(id = 1, stayId = 10, isPrimary = false)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)
        val saved = existing.copy(caption = "New caption", displayOrder = 2)
        Mockito.`when`(stayPictureRepository.save(Mockito.any(StayPicture::class.java))).thenReturn(saved)

        val result = service.updatePictureMetadata(10, 1, "New caption", false, 2)

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updatePictureMetadataReturnsNotFoundWhenMissing() {
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(10, 99, null, false, 0)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updatePictureMetadataRejectsDuplicatePrimaryWhenCurrentIsNot() {
        val existing = picture(id = 1, stayId = 10, isPrimary = false)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)
        Mockito.`when`(stayPictureRepository.existsByStayIdAndIsPrimary(10, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updatePictureMetadata(10, 1, null, isPrimary = true, displayOrder = 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- deletePicture ----

    @Test
    fun deletePictureReturnsNotFoundWhenMissing() {
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deletePicture(10, 99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePictureInvokesRepository() {
        val existing = picture(id = 1, stayId = 10)
        Mockito.`when`(stayPictureRepository.findByStayIdAndId(10, 1)).thenReturn(existing)

        service.deletePicture(10, 1)

        Mockito.verify(stayPictureRepository).deleteById(1)
    }
}

private fun StayPicture.copy(caption: String? = this.caption, displayOrder: Int = this.displayOrder) =
    StayPicture(id = id, stayId = stayId, url = url, caption = caption,
        isPrimary = isPrimary, displayOrder = displayOrder)
