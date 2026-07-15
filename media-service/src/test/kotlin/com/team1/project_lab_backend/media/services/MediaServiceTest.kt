package com.team1.project_lab_backend.media.services

import com.team1.project_lab_backend.media.dto.UpdateMediaRequest
import com.team1.project_lab_backend.media.models.Media
import com.team1.project_lab_backend.media.models.MediaOwnerType
import com.team1.project_lab_backend.media.repositories.MediaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException

class MediaServiceTest {
    private val mediaRepository = Mockito.mock(MediaRepository::class.java)
    private val storageService = Mockito.mock(StorageService::class.java)

    private val service = MediaService(mediaRepository, storageService)

    private fun media(
        id: Int = 1,
        ownerType: MediaOwnerType = MediaOwnerType.STAY,
        ownerId: Int = 10,
        isPrimary: Boolean = false,
    ) = Media(
        id = id,
        ownerType = ownerType,
        ownerId = ownerId,
        url = "stays/$ownerId/photo.jpg",
        caption = null,
        isPrimary = isPrimary,
        displayOrder = 0,
    )

    private fun imageFile(name: String = "photo.jpg") = MockMultipartFile("file", name, "image/jpeg", ByteArray(8) { 0 })

    // ---- addMedia ----

    @Test
    fun addMediaRejectsEmptyFile() {
        val emptyFile = MockMultipartFile("file", "photo.jpg", "image/jpeg", ByteArray(0))

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addMedia(MediaOwnerType.STAY, 10, emptyFile, null, false, 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addMediaRejectsNonImageContentType() {
        val pdf = MockMultipartFile("file", "doc.pdf", "application/pdf", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addMedia(MediaOwnerType.STAY, 10, pdf, null, false, 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addMediaRejectsDisallowedExtension() {
        val html = MockMultipartFile("file", "exploit.html", "image/jpeg", ByteArray(8) { 0 })

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addMedia(MediaOwnerType.STAY, 10, html, null, false, 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addMediaRejectsNegativeDisplayOrder() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addMedia(MediaOwnerType.STAY, 10, imageFile(), null, false, -1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addMediaRejectsDuplicatePrimary() {
        Mockito.`when`(mediaRepository.existsByOwnerTypeAndOwnerIdAndIsPrimary(MediaOwnerType.STAY, 10, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.addMedia(MediaOwnerType.STAY, 10, imageFile(), null, isPrimary = true, displayOrder = 0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun addMediaSavesUnderOwnerTypeFolder() {
        val file = imageFile()
        Mockito.`when`(storageService.save(file, "rooms/5")).thenReturn("rooms/5/photo.jpg")
        Mockito.`when`(mediaRepository.save(Mockito.any(Media::class.java))).thenAnswer { it.arguments[0] }
        Mockito.`when`(storageService.toUrl("rooms/5/photo.jpg")).thenReturn("http://localhost:8080/uploads/rooms/5/photo.jpg")

        val result = service.addMedia(MediaOwnerType.ROOM, 5, file, null, false, 0)

        assertEquals("http://localhost:8080/uploads/rooms/5/photo.jpg", result.url)
        assertEquals("ROOM", result.ownerType)
        assertEquals(5, result.ownerId)
    }

    // ---- updateMedia ----

    @Test
    fun updateMediaReturnsUpdated() {
        val existing = media(id = 1, isPrimary = false)
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 10, 1)).thenReturn(existing)
        Mockito.`when`(mediaRepository.save(Mockito.any(Media::class.java))).thenAnswer { it.arguments[0] }
        Mockito.`when`(storageService.toUrl(existing.url)).thenReturn("http://localhost:8080/uploads/${existing.url}")

        val result = service.updateMedia(MediaOwnerType.STAY, 10, 1, UpdateMediaRequest("New caption", false, 2))

        assertEquals("New caption", result.caption)
        assertEquals(2, result.displayOrder)
    }

    @Test
    fun updateMediaReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 10, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateMedia(MediaOwnerType.STAY, 10, 99, UpdateMediaRequest(null, false, 0))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateMediaReturnsNotFoundWhenIdBelongsToDifferentOwner() {
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 999, 1)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateMedia(MediaOwnerType.STAY, 999, 1, UpdateMediaRequest(null, false, 0))
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun updateMediaRejectsDuplicatePrimaryWhenCurrentIsNot() {
        val existing = media(id = 1, isPrimary = false)
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 10, 1)).thenReturn(existing)
        Mockito.`when`(mediaRepository.existsByOwnerTypeAndOwnerIdAndIsPrimary(MediaOwnerType.STAY, 10, true)).thenReturn(true)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.updateMedia(MediaOwnerType.STAY, 10, 1, UpdateMediaRequest(null, isPrimary = true, displayOrder = 0))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    // ---- deleteMedia ----

    @Test
    fun deleteMediaReturnsNotFoundWhenMissing() {
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 10, 99)).thenReturn(null)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deleteMedia(MediaOwnerType.STAY, 10, 99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deleteMediaInvokesRepositoryAndStorage() {
        val existing = media(id = 1)
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdAndId(MediaOwnerType.STAY, 10, 1)).thenReturn(existing)

        service.deleteMedia(MediaOwnerType.STAY, 10, 1)

        Mockito.verify(mediaRepository).deleteById(1)
        Mockito.verify(storageService).delete(existing.url)
    }

    // ---- listForOwners (bulk, backs the batch resolver) ----

    @Test
    fun listForOwnersGroupsAcrossMultipleOwners() {
        Mockito.`when`(mediaRepository.findByOwnerTypeAndOwnerIdIn(MediaOwnerType.STAY, listOf(10, 20)))
            .thenReturn(listOf(media(id = 1, ownerId = 10), media(id = 2, ownerId = 20)))
        Mockito.`when`(storageService.toUrl(Mockito.anyString())).thenAnswer { "http://localhost:8080/uploads/${it.arguments[0]}" }

        val result = service.listForOwners(MediaOwnerType.STAY, listOf(10, 20))

        assertEquals(2, result.size)
        assertEquals(setOf(10, 20), result.map { it.ownerId }.toSet())
    }
}
