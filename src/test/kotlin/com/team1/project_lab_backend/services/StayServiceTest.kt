package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.repositories.AccessibilityRepository
import com.team1.project_lab_backend.repositories.AmenityRepository
import com.team1.project_lab_backend.repositories.HostRepository
import com.team1.project_lab_backend.repositories.MealPlanRepository
import com.team1.project_lab_backend.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.repositories.PropertyBrandRepository
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

    private val stayService = StayService(
        stayRepository,
        hostRepository,
        propertyBrandRepository,
        viewRepository,
        amenityRepository,
        accessibilityRepository,
        mealPlanRepository,
        paymentTypeRepository,
        travelerExperienceRepository
    )

    private fun baseRequest(): StayRequest =
        StayRequest(
            price = BigDecimal("120.00"),
            name = "Test Stay",
            streetAddress = "123 Main",
            city = "Testville",
            sleeps = 2,
            bedroomAmount = 1,
            bathrooms = BigDecimal("1.0"),
            hostId = 1
        )

    private fun stubHost(id: Int = 1): Host {
        val host = Host(id = id)
        Mockito.`when`(hostRepository.findById(id)).thenReturn(Optional.of(host))
        return host
    }

    private fun stubSave(request: StayRequest, host: Host, id: Int = 10): Stay {
        val stay = Stay(
            id = id,
            price = request.price,
            name = request.name,
            about = request.about,
            propertyType = request.propertyType,
            streetAddress = request.streetAddress,
            extendedAddress = request.extendedAddress,
            city = request.city,
            stateProvince = request.stateProvince,
            postalCode = request.postalCode,
            countryCode = request.countryCode,
            isAvailable = request.isAvailable,
            isRefundable = request.isRefundable,
            starRating = request.starRating,
            sleeps = request.sleeps,
            bedroomAmount = request.bedroomAmount,
            bathrooms = request.bathrooms,
            size = request.size,
            daysFromBookingCancellationDeadline = request.daysFromBookingCancellationDeadline,
            policiesText = request.policiesText,
            importantInformation = request.importantInformation,
            host = host
        )
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenReturn(stay)
        return stay
    }

    @Test
    fun createStayRejectsNegativePrice() {
        val request = baseRequest().copy(price = BigDecimal("-1.00"))

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
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
    fun createStayAllowsZeroPrice() {
        val request = baseRequest().copy(price = BigDecimal.ZERO)
        val host = stubHost()
        stubSave(request, host, id = 11)

        val response = stayService.createStay(request)

        assertEquals(11, response.id)
        assertEquals(BigDecimal.ZERO, response.price)
    }

    @Test
    fun createStayRejectsZeroSleeps() {
        val request = baseRequest().copy(sleeps = 0)

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
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
        val request = baseRequest().copy(streetAddress = "")

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankCity() {
        val request = baseRequest().copy(city = " ")

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.createStay(request)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsNegativeBathrooms() {
        val request = baseRequest().copy(bathrooms = BigDecimal("-0.5"))

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
        Mockito.`when`(stayRepository.existsById(55)).thenReturn(false)

        val exception = assertThrows(ResponseStatusException::class.java) {
            stayService.updateStay(55, request)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun updateStayIdempotentForSameData() {
        val request = baseRequest()
        Mockito.`when`(stayRepository.existsById(20)).thenReturn(true)
        val host = stubHost()
        stubSave(request, host, id = 20)

        val first = stayService.updateStay(20, request)
        val second = stayService.updateStay(20, request)

        assertEquals(first.id, second.id)
        assertEquals(first.name, second.name)
        assertEquals(first.price, second.price)
    }
}
