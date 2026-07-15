package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AddressRequest
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.AccessibilityRepository
import com.team1.project_lab_backend.inventory.repositories.AmenityRepository
import com.team1.project_lab_backend.inventory.repositories.MealPlanRepository
import com.team1.project_lab_backend.inventory.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.inventory.repositories.PropertyBrandRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.inventory.repositories.TravelerExperienceRepository
import com.team1.project_lab_backend.inventory.repositories.ViewRepository
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
    private val hostFeignClient = Mockito.mock(HostFeignClient::class.java)
    private val bookingAvailabilityClient = Mockito.mock(BookingAvailabilityClient::class.java)
    private val propertyBrandRepository = Mockito.mock(PropertyBrandRepository::class.java)
    private val viewRepository = Mockito.mock(ViewRepository::class.java)
    private val amenityRepository = Mockito.mock(AmenityRepository::class.java)
    private val accessibilityRepository = Mockito.mock(AccessibilityRepository::class.java)
    private val mealPlanRepository = Mockito.mock(MealPlanRepository::class.java)
    private val paymentTypeRepository = Mockito.mock(PaymentTypeRepository::class.java)
    private val travelerExperienceRepository = Mockito.mock(TravelerExperienceRepository::class.java)
    private val stayService =
        StayService(
            stayRepository,
            hostFeignClient,
            bookingAvailabilityClient,
            propertyBrandRepository,
            viewRepository,
            amenityRepository,
            accessibilityRepository,
            mealPlanRepository,
            paymentTypeRepository,
            travelerExperienceRepository,
        )

    private fun baseAddress(): AddressRequest =
        AddressRequest(
            streetAddress = "123 Main",
            city = "Testville",
            countryCode = "US",
        )

    private fun baseRequest(): StayRequest =
        StayRequest(
            name = "Test Stay",
            propertyType = PropertyType.HOME,
            address = baseAddress(),
            hostId = 1,
        )

    private fun stubHost(id: Int = 1): HostRef {
        val host = HostRef(id = id)
        Mockito.`when`(hostFeignClient.get(id)).thenReturn(host)
        return host
    }

    private fun stubSave(
        request: StayRequest,
        hostId: Int,
        id: Int = 10,
    ): Stay {
        val address =
            Address(
                id = 0,
                streetAddress = request.address.streetAddress,
                city = request.address.city,
                countryCode = request.address.countryCode,
            )
        val stay =
            Stay(
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
                hostId = hostId,
            )
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenReturn(stay)
        return stay
    }

    @Test
    fun createStayReturnsPersistedStay() {
        val request = baseRequest()
        val host = stubHost()
        stubSave(request, host.id, id = 10)

        val response = stayService.createStay(request, 1)

        assertEquals(10, response.id)
        assertEquals("Test Stay", response.name)
        assertEquals(1, response.hostId)
    }

    @Test
    fun createStayRejectsBlankName() {
        val request = baseRequest().copy(name = " ")

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankStreetAddress() {
        val request = baseRequest().copy(address = baseAddress().copy(streetAddress = ""))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankCity() {
        val request = baseRequest().copy(address = baseAddress().copy(city = " "))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsBlankCountryCode() {
        val request = baseRequest().copy(address = baseAddress().copy(countryCode = ""))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsStarRatingAboveFive() {
        val request = baseRequest().copy(starRating = BigDecimal("5.1"))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayAllowsStarRatingAtFive() {
        val request = baseRequest().copy(starRating = BigDecimal("5.0"))
        val host = stubHost()
        stubSave(request, host.id, id = 12)

        val response = stayService.createStay(request, 1)

        assertEquals(12, response.id)
        assertEquals(BigDecimal("5.0"), response.starRating)
    }

    @Test
    fun createStayRejectsHostIdZero() {
        val request = baseRequest().copy(hostId = 0)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 0)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsViewIdsWithZero() {
        val request = baseRequest().copy(viewIds = setOf(0))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun createStayRejectsLatitudeOutOfRange() {
        val request = baseRequest().copy(latitude = 91.0, longitude = 0.0)
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createStayRejectsLongitudeOutOfRange() {
        val request = baseRequest().copy(latitude = 0.0, longitude = 181.0)
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun createStayRejectsLatitudeWithoutLongitude() {
        val request = baseRequest().copy(latitude = 10.0, longitude = null)
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.createStay(request, 1)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsCheckInWithoutCheckOut() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(checkIn = java.time.LocalDate.now().plusDays(1)))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsCheckOutWithoutCheckIn() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(checkOut = java.time.LocalDate.now().plusDays(2)))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsCheckOutNotAfterCheckIn() {
        val today = java.time.LocalDate.now()
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(checkIn = today.plusDays(2), checkOut = today.plusDays(1)))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsNegativeMinPrice() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(minPricePerNight = BigDecimal("-1")))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsNegativeMaxPrice() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(maxPricePerNight = BigDecimal("-0.01")))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsMinPriceAboveMaxPrice() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(
                    StayFilter(minPricePerNight = BigDecimal("200"), maxPricePerNight = BigDecimal("100")),
                )
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun searchStaysRejectsGuestsLessThanOne() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.searchStays(StayFilter(guests = 0))
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun updateStayReturnsNotFoundWhenMissing() {
        val request = baseRequest()
        Mockito.`when`(stayRepository.findById(55)).thenReturn(Optional.empty())

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                stayService.updateStay(55, request, 1)
            }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun updateStayIdempotentForSameData() {
        val request = baseRequest()
        val host = stubHost()
        val address = Address(id = 5, streetAddress = "123 Main", city = "Testville", countryCode = "US")
        val existingStay =
            Stay(
                id = 20,
                name = request.name,
                propertyType = request.propertyType,
                address = address,
                hostId = host.id,
            )
        Mockito.`when`(stayRepository.findById(20)).thenReturn(Optional.of(existingStay))
        stubSave(request, host.id, id = 20)

        val first = stayService.updateStay(20, request, 1)
        val second = stayService.updateStay(20, request, 1)

        assertEquals(first.id, second.id)
        assertEquals(first.name, second.name)
    }
}
