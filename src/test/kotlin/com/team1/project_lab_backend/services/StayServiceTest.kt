package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AddressRequest
import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.PropertyType
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.repositories.AccessibilityRepository
import com.team1.project_lab_backend.repositories.AmenityRepository
import com.team1.project_lab_backend.repositories.HostRepository
import com.team1.project_lab_backend.repositories.MealPlanRepository
import com.team1.project_lab_backend.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.repositories.PropertyBrandRepository
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
import com.team1.project_lab_backend.repositories.TravelerExperienceRepository
import com.team1.project_lab_backend.repositories.ViewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

class StayServiceTest {
    private val stayRepository = Mockito.mock(StayRepository::class.java)
    private val hostRepository = Mockito.mock(HostRepository::class.java)
    private val propertyBrandRepository = Mockito.mock(PropertyBrandRepository::class.java)
    private val viewRepository = Mockito.mock(ViewRepository::class.java)
    private val amenityRepository = Mockito.mock(AmenityRepository::class.java)
    private val accessibilityRepository = Mockito.mock(AccessibilityRepository::class.java)
    private val mealPlanRepository = Mockito.mock(MealPlanRepository::class.java)
    private val paymentTypeRepository = Mockito.mock(PaymentTypeRepository::class.java)
    private val travelerExperienceRepository = Mockito.mock(TravelerExperienceRepository::class.java)
    private val roomRepository = Mockito.mock(RoomRepository::class.java)
    private val stayPictureRepository = Mockito.mock(StayPictureRepository::class.java)

    private val stayService = StayService(
        stayRepository,
        hostRepository,
        propertyBrandRepository,
        viewRepository,
        amenityRepository,
        accessibilityRepository,
        mealPlanRepository,
        paymentTypeRepository,
        travelerExperienceRepository,
        roomRepository,
        stayPictureRepository
    )

    private fun baseAddress(): AddressRequest =
        AddressRequest(
            streetAddress = "123 Main",
            city = "Testville",
            countryCode = "US"
        )

    private fun baseRequest(): StayRequest =
        StayRequest(
            name = "Test Stay",
            propertyType = PropertyType.HOME,
            address = baseAddress(),
            hostId = 1
        )

    private fun stubHost(id: Int = 1): Host {
        val host = Host(id = id)
        Mockito.`when`(hostRepository.findById(id)).thenReturn(Optional.of(host))
        return host
    }

    private fun stubSave(request: StayRequest, host: Host, id: Int = 10): Stay {
        val address = Address(
            id = 0,
            streetAddress = request.address.streetAddress,
            city = request.address.city,
            countryCode = request.address.countryCode
        )
        val stay = Stay(
            id = id,
            name = request.name,
            about = request.about,
            propertyType = request.propertyType,
            address = address,
            isRefundable = request.isRefundable,
            starRating = request.starRating,
            daysFromBookingCancellationDeadline = request.daysFromBookingCancellationDeadline,
            policiesText = request.policiesText,
            importantInformation = request.importantInformation,
            host = host
        )
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenReturn(stay)
        Mockito.`when`(roomRepository.findByStayId(id)).thenReturn(emptyList())
        Mockito.`when`(stayPictureRepository.findByStayId(id)).thenReturn(emptyList())
        return stay
    }

    @Test
    fun createStayReturnsPersistedStay() {
        val request = baseRequest()
        val host = stubHost()
        stubSave(request, host, id = 10)

        val response = stayService.createStay(request)

        assertEquals(10, response.id)
        assertEquals("Test Stay", response.name)
        assertEquals(1, response.hostId)
    }

    @Test
    fun createStayRejectsBlankName() {
        val request = baseRequest().copy(name = " ")

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankStreetAddress() {
        val request = baseRequest().copy(address = baseAddress().copy(streetAddress = ""))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankCity() {
        val request = baseRequest().copy(address = baseAddress().copy(city = " "))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankCountryCode() {
        val request = baseRequest().copy(address = baseAddress().copy(countryCode = ""))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsStarRatingAboveFive() {
        val request = baseRequest().copy(starRating = BigDecimal("5.1"))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayAllowsStarRatingAtFive() {
        val request = baseRequest().copy(starRating = BigDecimal("5.0"))
        val host = stubHost()
        stubSave(request, host, id = 12)

        val response = stayService.createStay(request)

        assertEquals(12, response.id)
        assertEquals(BigDecimal("5.0"), response.starRating)
    }

    @Test
    fun createStayRejectsHostIdZero() {
        val request = baseRequest().copy(hostId = 0)

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsViewIdsWithZero() {
        val request = baseRequest().copy(viewIds = setOf(0))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun updateStayReturnsNotFoundWhenMissing() {
        val request = baseRequest()
        Mockito.`when`(stayRepository.findById(55)).thenReturn(Optional.empty())

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.updateStay(55, request)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun updateStayIdempotentForSameData() {
        val request = baseRequest()
        val host = stubHost()
        val address = Address(id = 5, streetAddress = "123 Main", city = "Testville", countryCode = "US")
        val existingStay = Stay(
            id = 20,
            name = request.name,
            propertyType = request.propertyType,
            address = address,
            host = host
        )
        Mockito.`when`(stayRepository.findById(20)).thenReturn(Optional.of(existingStay))
        stubSave(request, host, id = 20)

        val first = stayService.updateStay(20, request)
        val second = stayService.updateStay(20, request)

        assertEquals(first.id, second.id)
        assertEquals(first.name, second.name)
    }
}
