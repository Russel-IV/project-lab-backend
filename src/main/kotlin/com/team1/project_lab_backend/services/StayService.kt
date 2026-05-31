package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.dto.StayResponse
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
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StayService(
    private val stayRepository: StayRepository,
    private val hostRepository: HostRepository,
    private val propertyBrandRepository: PropertyBrandRepository,
    private val viewRepository: ViewRepository,
    private val amenityRepository: AmenityRepository,
    private val accessibilityRepository: AccessibilityRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val travelerExperienceRepository: TravelerExperienceRepository
) {
    @Transactional(readOnly = true)
    fun getAllStays(): List<StayResponse> =
        stayRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getStayById(id: Int): StayResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return stayRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found") }
    }

    @Transactional
    fun createStay(request: StayRequest): StayResponse {
        if (request.price.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be >= 0")
        }
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (request.streetAddress.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "streetAddress must not be blank")
        }
        if (request.city.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "city must not be blank")
        }
        if (request.hostId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId must be positive")
        }
        if (request.bathrooms.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bathrooms must be >= 0")
        }
        if (request.sleeps <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sleeps must be > 0")
        }
        if (request.bedroomAmount < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bedroomAmount must be >= 0")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be >= 0")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal("5.0")) > 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be <= 5.0")
        }
        if (request.size != null && request.size.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be >= 0")
        }
        if (request.daysFromBookingCancellationDeadline != null && request.daysFromBookingCancellationDeadline < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "daysFromBookingCancellationDeadline must be >= 0"
            )
        }
        if (request.propertyBrandId != null && request.propertyBrandId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "propertyBrandId must be positive")
        }
        if (request.viewIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "viewIds must contain only positive ids")
        }
        if (request.amenityIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amenityIds must contain only positive ids")
        }
        if (request.accessibilityIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "accessibilityIds must contain only positive ids")
        }
        if (request.mealPlanIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "mealPlanIds must contain only positive ids")
        }
        if (request.paymentTypeIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentTypeIds must contain only positive ids")
        }
        if (request.travelerExperienceIds.any { it <= 0 }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "travelerExperienceIds must contain only positive ids"
            )
        }
        val stay = buildStay(0, request)
        return stayRepository.save(stay).toResponse()
    }

    @Transactional
    fun updateStay(id: Int, request: StayRequest): StayResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.price.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be >= 0")
        }
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (request.streetAddress.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "streetAddress must not be blank")
        }
        if (request.city.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "city must not be blank")
        }
        if (request.hostId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId must be positive")
        }
        if (request.bathrooms.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bathrooms must be >= 0")
        }
        if (request.sleeps <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sleeps must be > 0")
        }
        if (request.bedroomAmount < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bedroomAmount must be >= 0")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be >= 0")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal("5.0")) > 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be <= 5.0")
        }
        if (request.size != null && request.size.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be >= 0")
        }
        if (request.daysFromBookingCancellationDeadline != null && request.daysFromBookingCancellationDeadline < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "daysFromBookingCancellationDeadline must be >= 0"
            )
        }
        if (request.propertyBrandId != null && request.propertyBrandId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "propertyBrandId must be positive")
        }
        if (request.viewIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "viewIds must contain only positive ids")
        }
        if (request.amenityIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amenityIds must contain only positive ids")
        }
        if (request.accessibilityIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "accessibilityIds must contain only positive ids")
        }
        if (request.mealPlanIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "mealPlanIds must contain only positive ids")
        }
        if (request.paymentTypeIds.any { it <= 0 }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentTypeIds must contain only positive ids")
        }
        if (request.travelerExperienceIds.any { it <= 0 }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "travelerExperienceIds must contain only positive ids"
            )
        }
        if (!stayRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
        val stay = buildStay(id, request)
        return stayRepository.save(stay).toResponse()
    }

    @Transactional
    fun deleteStay(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!stayRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found")
        }
        stayRepository.deleteById(id)
    }

    private fun buildStay(id: Int, request: StayRequest): Stay {
        val host = hostRepository.findById(request.hostId).orElseThrow {
            ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId not found")
        }
        val propertyBrand = request.propertyBrandId?.let { brandId ->
            propertyBrandRepository.findById(brandId).orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "propertyBrandId not found")
            }
        }
        val views = fetchAllByIds(request.viewIds, viewRepository, "viewIds")
        val amenities = fetchAllByIds(request.amenityIds, amenityRepository, "amenityIds")
        val accessibilities = fetchAllByIds(request.accessibilityIds, accessibilityRepository, "accessibilityIds")
        val mealPlans = fetchAllByIds(request.mealPlanIds, mealPlanRepository, "mealPlanIds")
        val paymentTypes = fetchAllByIds(request.paymentTypeIds, paymentTypeRepository, "paymentTypeIds")
        val travelerExperiences = fetchAllByIds(
            request.travelerExperienceIds,
            travelerExperienceRepository,
            "travelerExperienceIds"
        )

        return Stay(
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
            host = host,
            propertyBrand = propertyBrand,
            views = views,
            amenities = amenities,
            accessibilities = accessibilities,
            mealPlans = mealPlans,
            paymentTypes = paymentTypes,
            travelerExperiences = travelerExperiences
        )
    }

    private fun <T : Any> fetchAllByIds(
        ids: Set<Int>,
        repository: JpaRepository<T, Int>,
        fieldName: String
    ): MutableSet<T> {
        if (ids.isEmpty()) {
            return mutableSetOf()
        }
        val entities = repository.findAllById(ids).toList()
        if (entities.size != ids.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName contains unknown ids")
        }
        return entities.toMutableSet()
    }
}

private fun Stay.toResponse(): StayResponse =
    StayResponse(
        id = id,
        price = price,
        name = name,
        about = about,
        propertyType = propertyType,
        streetAddress = streetAddress,
        extendedAddress = extendedAddress,
        city = city,
        stateProvince = stateProvince,
        postalCode = postalCode,
        countryCode = countryCode,
        isAvailable = isAvailable,
        isRefundable = isRefundable,
        starRating = starRating,
        sleeps = sleeps,
        bedroomAmount = bedroomAmount,
        bathrooms = bathrooms,
        size = size,
        daysFromBookingCancellationDeadline = daysFromBookingCancellationDeadline,
        policiesText = policiesText,
        importantInformation = importantInformation,
        hostId = host.id,
        propertyBrandId = propertyBrand?.id,
        viewIds = views.map { it.id }.toSet(),
        amenityIds = amenities.map { it.id }.toSet(),
        accessibilityIds = accessibilities.map { it.id }.toSet(),
        mealPlanIds = mealPlans.map { it.id }.toSet(),
        paymentTypeIds = paymentTypes.map { it.id }.toSet(),
        travelerExperienceIds = travelerExperiences.map { it.id }.toSet()
    )
