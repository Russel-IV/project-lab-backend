package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.AddressResponse
import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.dto.StayResponse
import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.models.StayPicture
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
    private val travelerExperienceRepository: TravelerExperienceRepository,
    private val roomRepository: RoomRepository,
    private val stayPictureRepository: StayPictureRepository
) {
    @Transactional(readOnly = true)
    fun getAllStays(): List<StayResponse> {
        val stays = stayRepository.findAll()
        if (stays.isEmpty()) return emptyList()
        val stayIds = stays.map { it.id }
        val roomsByStayId = roomRepository.findByStayIdIn(stayIds).groupBy { it.stayId }
        val picturesByStayId = stayPictureRepository.findByStayIdIn(stayIds).groupBy { it.stayId }
        return stays.map { stay ->
            stay.toResponse(
                rooms = roomsByStayId[stay.id] ?: emptyList(),
                pictures = picturesByStayId[stay.id] ?: emptyList()
            )
        }
    }

    @Transactional(readOnly = true)
    fun getStayById(id: Int): StayResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        val stay = stayRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found") }
        val rooms = roomRepository.findByStayId(id)
        val pictures = stayPictureRepository.findByStayId(id)
        return stay.toResponse(rooms = rooms, pictures = pictures)
    }

    @Transactional
    fun createStay(request: StayRequest): StayResponse {
        validateStayRequest(request)
        val stay = buildStay(0, request, existingAddressId = 0)
        val saved = stayRepository.save(stay)
        val rooms = roomRepository.findByStayId(saved.id)
        val pictures = stayPictureRepository.findByStayId(saved.id)
        return saved.toResponse(rooms = rooms, pictures = pictures)
    }

    @Transactional
    fun updateStay(id: Int, request: StayRequest): StayResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        validateStayRequest(request)
        val existingStay = stayRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found") }
        val existingAddressId = existingStay.address.id
        val stay = buildStay(id, request, existingAddressId = existingAddressId)
        val saved = stayRepository.save(stay)
        val rooms = roomRepository.findByStayId(saved.id)
        val pictures = stayPictureRepository.findByStayId(saved.id)
        return saved.toResponse(rooms = rooms, pictures = pictures)
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

    private fun validateStayRequest(request: StayRequest) {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (request.address.streetAddress.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "streetAddress must not be blank")
        }
        if (request.address.city.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "city must not be blank")
        }
        if (request.address.countryCode.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "countryCode must not be blank")
        }
        if (request.hostId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId must be positive")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be >= 0")
        }
        if (request.starRating != null && request.starRating.compareTo(java.math.BigDecimal("5.0")) > 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRating must be <= 5.0")
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
    }

    private fun buildStay(id: Int, request: StayRequest, existingAddressId: Int = 0): Stay {
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

        val address = Address(
            id = existingAddressId,
            streetAddress = request.address.streetAddress,
            extendedAddress = request.address.extendedAddress,
            city = request.address.city,
            stateProvince = request.address.stateProvince,
            postalCode = request.address.postalCode,
            countryCode = request.address.countryCode
        )

        return Stay(
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

private fun Stay.toResponse(rooms: List<Room>, pictures: List<StayPicture>): StayResponse =
    StayResponse(
        id = id,
        name = name,
        about = about,
        propertyType = propertyType,
        address = address.toResponse(),
        isRefundable = isRefundable,
        starRating = starRating,
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
        travelerExperienceIds = travelerExperiences.map { it.id }.toSet(),
        rooms = rooms.map { room ->
            com.team1.project_lab_backend.dto.RoomResponse(
                id = room.id,
                stayId = room.stayId,
                name = room.name,
                price = room.price,
                sleeps = room.sleeps,
                bedroomAmount = room.bedroomAmount,
                bathrooms = room.bathrooms,
                size = room.size
            )
        },
        pictures = pictures.map { pic ->
            com.team1.project_lab_backend.dto.StayPictureResponse(
                id = pic.id,
                stayId = pic.stayId,
                url = pic.url,
                caption = pic.caption,
                isPrimary = pic.isPrimary,
                displayOrder = pic.displayOrder
            )
        },
        startingFromPrice = rooms.map { it.price }.minOrNull()
    )

private fun Address.toResponse(): AddressResponse =
    AddressResponse(
        id = id,
        streetAddress = streetAddress,
        extendedAddress = extendedAddress,
        city = city,
        stateProvince = stateProvince,
        postalCode = postalCode,
        countryCode = countryCode
    )
