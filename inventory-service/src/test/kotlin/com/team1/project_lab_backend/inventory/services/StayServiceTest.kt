package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AddressRequest
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Region
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.repositories.AccessibilityRepository
import com.team1.project_lab_backend.inventory.repositories.AmenityRepository
import com.team1.project_lab_backend.inventory.repositories.MealPlanRepository
import com.team1.project_lab_backend.inventory.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.inventory.repositories.PropertyBrandRepository
import com.team1.project_lab_backend.inventory.repositories.RegionRepository
import com.team1.project_lab_backend.inventory.repositories.RoomRepository
import com.team1.project_lab_backend.inventory.repositories.StayRepository
import com.team1.project_lab_backend.inventory.repositories.TravelerExperienceRepository
import com.team1.project_lab_backend.inventory.repositories.ViewRepository
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

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
    private val regionRepository = Mockito.mock(RegionRepository::class.java)
    private val roomRepository = Mockito.mock(RoomRepository::class.java)
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
            regionRepository,
            roomRepository,
        )

    init {
        // Default: no existing region for any (city, countryCode) — findOrCreateRegion()
        // falls through to save(), which we echo back so buildStay() gets a non-null
        // Region. Tests exercising reuse override this with a specific stub.
        Mockito.`when`(regionRepository.save(Mockito.any(Region::class.java))).thenAnswer { it.arguments[0] }
    }

    private fun sampleRegion(
        id: Int = 1,
        city: String = "Testville",
        countryCode: String = "US",
    ) = Region(id = id, city = city, countryCode = countryCode)

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
                region = sampleRegion(city = request.address.city, countryCode = request.address.countryCode),
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
    fun createStayReusesExistingRegionForSameCityAndCountry() {
        val request = baseRequest()
        stubHost()
        val existingRegion = sampleRegion(id = 7)
        Mockito.`when`(regionRepository.findByCityIgnoreCaseAndCountryCodeIgnoreCase("Testville", "US"))
            .thenReturn(existingRegion)
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenAnswer { it.arguments[0] }

        val response = stayService.createStay(request, 1)

        assertEquals(7, response.address.regionId)
        Mockito.verify(regionRepository, Mockito.never()).save(Mockito.any(Region::class.java))
    }

    @Test
    fun createStayCreatesNewRegionForUnseenCityAndCountry() {
        val request = baseRequest()
        stubHost()
        Mockito.`when`(regionRepository.findByCityIgnoreCaseAndCountryCodeIgnoreCase("Testville", "US"))
            .thenReturn(null)
        Mockito.`when`(regionRepository.save(Mockito.any(Region::class.java))).thenReturn(sampleRegion(id = 9))
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenAnswer { it.arguments[0] }

        val response = stayService.createStay(request, 1)

        assertEquals(9, response.address.regionId)
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

    // ---- getPriceStats ----

    @Test
    fun getPriceStatsBucketsPricesAcrossEqualWidthBins() {
        val stays = listOf(sampleStayEntity(1), sampleStayEntity(2), sampleStayEntity(3), sampleStayEntity(4))
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>())).thenReturn(stays)
        // One value comfortably inside each of the 4 equal-width buckets over [10, 90]
        // ([10,30), [30,50), [50,70), [70,90]) -- the last ("90", the max itself) lands
        // exactly on `bins` before being clamped into the final bucket, which is the
        // deliberate coerceIn behavior under test, not incidental.
        val prices = listOf(BigDecimal("10"), BigDecimal("35"), BigDecimal("60"), BigDecimal("90"))
        Mockito.`when`(roomRepository.findMinPricePerStay(listOf(1, 2, 3, 4))).thenReturn(prices)

        val result = stayService.getPriceStats(StayFilter(), bins = 4)

        assertEquals(BigDecimal("10"), result.min)
        assertEquals(BigDecimal("90"), result.max)
        assertEquals(4, result.count)
        assertEquals(listOf(1, 1, 1, 1), result.histogram)
    }

    @Test
    fun getPriceStatsReturnsEmptyStatsWhenNoStaysMatch() {
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>())).thenReturn(emptyList())

        val result = stayService.getPriceStats(StayFilter(), bins = 10)

        assertEquals(null, result.min)
        assertEquals(null, result.max)
        assertEquals(0, result.count)
        assertEquals(List(10) { 0 }, result.histogram)
        Mockito.verify(roomRepository, Mockito.never()).findMinPricePerStay(Mockito.anyList())
    }

    @Test
    fun getPriceStatsReturnsEmptyStatsWhenMatchingStaysHaveNoRooms() {
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>())).thenReturn(listOf(sampleStayEntity(1)))
        Mockito.`when`(roomRepository.findMinPricePerStay(listOf(1))).thenReturn(emptyList())

        val result = stayService.getPriceStats(StayFilter(), bins = 10)

        assertEquals(null, result.min)
        assertEquals(null, result.max)
        assertEquals(0, result.count)
        assertEquals(List(10) { 0 }, result.histogram)
    }

    @Test
    fun getPriceStatsPutsAllPricesInBucketZeroWhenAllEqual() {
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>()))
            .thenReturn(listOf(sampleStayEntity(1), sampleStayEntity(2)))
        Mockito.`when`(roomRepository.findMinPricePerStay(listOf(1, 2)))
            .thenReturn(listOf(BigDecimal("50"), BigDecimal("50")))

        val result = stayService.getPriceStats(StayFilter(), bins = 5)

        assertEquals(BigDecimal("50"), result.min)
        assertEquals(BigDecimal("50"), result.max)
        assertEquals(2, result.count)
        assertEquals(listOf(2, 0, 0, 0, 0), result.histogram)
    }

    @Test
    fun getPriceStatsRejectsBinsLessThanOne() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.getPriceStats(StayFilter(), bins = 0)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getPriceStatsRejectsBinsAboveMax() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.getPriceStats(StayFilter(), bins = 1001)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun getPriceStatsReusesFilterValidation() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                stayService.getPriceStats(StayFilter(minPricePerNight = BigDecimal("-1")), bins = 10)
            }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    private fun sampleStayEntity(id: Int) =
        Stay(
            id = id,
            name = "Test Stay $id",
            propertyType = PropertyType.HOME,
            address = Address(id = id, streetAddress = "123 Main", city = "Testville", countryCode = "US", region = sampleRegion()),
            hostId = 1,
        )

    @Test
    fun searchStaysAppliesStableIdSortAndPaginationMetadata() {
        val page = PageImpl(listOf(sampleStayEntity(1), sampleStayEntity(2)), PageRequest.of(0, 20), 42L)
        val pageableCaptor = ArgumentCaptor.forClass(Pageable::class.java)
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>(), pageableCaptor.capture()))
            .thenReturn(page)

        val result = stayService.searchStays(StayFilter(), page = 0, size = 20)

        assertEquals(Sort.by(Sort.Direction.ASC, "id"), pageableCaptor.value.sort)
        assertEquals(2, result.items.size)
        assertEquals(42L, result.totalCount)
        assertEquals(true, result.hasNextPage)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun searchStaysBuildsIsRefundablePredicateWhenFilterSet() {
        val page = PageImpl<Stay>(emptyList())
        Mockito.`when`(stayRepository.findAll(Mockito.any<Specification<Stay>>(), Mockito.any(Pageable::class.java)))
            .thenReturn(page)

        stayService.searchStays(StayFilter(isRefundable = true))

        val specCaptor = ArgumentCaptor.forClass(Specification::class.java) as ArgumentCaptor<Specification<Stay>>
        Mockito.verify(stayRepository).findAll(specCaptor.capture(), Mockito.any(Pageable::class.java))
        val spec = specCaptor.value

        val root = Mockito.mock(Root::class.java) as Root<Stay>
        val query = Mockito.mock(CriteriaQuery::class.java) as CriteriaQuery<Stay>
        val cb = Mockito.mock(CriteriaBuilder::class.java)
        val path = Mockito.mock(Path::class.java) as Path<Boolean>
        Mockito.`when`(root.get<Boolean>("isRefundable")).thenReturn(path)

        spec.toPredicate(root, query, cb)

        Mockito.verify(cb).equal(path, true)
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
        val address = Address(id = 5, streetAddress = "123 Main", city = "Testville", countryCode = "US", region = sampleRegion())
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

    @Test
    fun updateStayPreservesPublicId() {
        val request = baseRequest()
        val host = stubHost()
        val address = Address(id = 5, streetAddress = "123 Main", city = "Testville", countryCode = "US", region = sampleRegion())
        val existingStay =
            Stay(
                id = 20,
                name = request.name,
                propertyType = request.propertyType,
                address = address,
                hostId = host.id,
            )
        Mockito.`when`(stayRepository.findById(20)).thenReturn(Optional.of(existingStay))
        Mockito.`when`(stayRepository.save(Mockito.any(Stay::class.java))).thenAnswer { it.arguments[0] }

        val result = stayService.updateStay(20, request, 1)

        assertEquals(existingStay.publicId, result.publicId)
    }

    @Test
    fun getStayByPublicIdReturnsNotFoundWhenMissing() {
        val publicId = UUID.randomUUID()
        Mockito.`when`(stayRepository.findByPublicId(publicId)).thenReturn(Optional.empty())

        val ex = assertThrows(ResponseStatusException::class.java) { stayService.getStayByPublicId(publicId) }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun getStayByPublicIdDelegatesToRepository() {
        val stay = sampleStayEntity(10)
        Mockito.`when`(stayRepository.findByPublicId(stay.publicId)).thenReturn(Optional.of(stay))

        val result = stayService.getStayByPublicId(stay.publicId)

        assertEquals(stay.publicId, result.publicId)
    }
}
