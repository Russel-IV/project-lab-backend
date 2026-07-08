package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.StayFilter
import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.models.Address
import com.team1.project_lab_backend.models.Amenity
import com.team1.project_lab_backend.models.AmenityType
import com.team1.project_lab_backend.models.Booking
import org.locationtech.jts.geom.Point
import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
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
import com.team1.project_lab_backend.util.orBadRequest
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireAllPositive
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireInRange
import com.team1.project_lab_backend.util.requireNonNegative
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

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
) {
    @Transactional(readOnly = true)
    fun searchStays(filter: StayFilter, page: Int = 0, size: Int = 20): List<Stay> {
        validateFilter(filter)
        val spec = buildSpec(filter)
        return stayRepository.findAll(spec, PageRequest.of(page, size)).content
    }

    @Transactional(readOnly = true)
    fun getStayById(id: Int): Stay {
        id.requirePositive()
        return stayRepository.findById(id).orNotFound("stay not found")
    }

    @Transactional
    fun createStay(request: StayRequest, requestingUserId: Int): Stay {
        if (request.hostId != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        validateStayRequest(request)
        return stayRepository.save(buildStay(0, request, existingAddressId = 0))
    }

    @Transactional
    fun updateStay(id: Int, request: StayRequest, requestingUserId: Int): Stay {
        id.requirePositive()
        validateStayRequest(request)
        val existingStay = stayRepository.findById(id).orNotFound("stay not found")
        if (existingStay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        return stayRepository.save(buildStay(id, request, existingAddressId = existingStay.address.id))
    }

    @Transactional
    fun deleteStay(id: Int, requestingUserId: Int) {
        id.requirePositive()
        val stay = stayRepository.findById(id).orNotFound("stay not found")
        if (stay.host.id != requestingUserId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        stayRepository.deleteById(id)
    }

    private fun validateStayRequest(request: StayRequest) {
        request.name.requireNotBlank("name")
        request.address.streetAddress.requireNotBlank("streetAddress")
        request.address.city.requireNotBlank("city")
        request.address.countryCode.requireNotBlank("countryCode")
        request.hostId.requirePositive("hostId")
        request.starRating?.requireInRange(BigDecimal.ZERO, BigDecimal("5.0"), "starRating")
        request.daysFromBookingCancellationDeadline?.requireNonNegative("daysFromBookingCancellationDeadline")
        request.propertyBrandId?.requirePositive("propertyBrandId")
        request.viewIds.requireAllPositive("viewIds")
        request.amenityIds.requireAllPositive("amenityIds")
        request.accessibilityIds.requireAllPositive("accessibilityIds")
        request.mealPlanIds.requireAllPositive("mealPlanIds")
        request.paymentTypeIds.requireAllPositive("paymentTypeIds")
        request.travelerExperienceIds.requireAllPositive("travelerExperienceIds")
        request.location?.let {
            if (it.y !in -90.0..90.0)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude must be between -90 and 90")
            if (it.x !in -180.0..180.0)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "longitude must be between -180 and 180")
        }
    }

    private fun buildStay(id: Int, request: StayRequest, existingAddressId: Int = 0): Stay {
        val host = hostRepository.findById(request.hostId).orBadRequest("hostId not found")
        val propertyBrand = request.propertyBrandId?.let { brandId ->
            propertyBrandRepository.findById(brandId).orBadRequest("propertyBrandId not found")
        }
        val views = fetchAllByIds(request.viewIds, viewRepository, "viewIds")
        val amenities = fetchAllByIds(request.amenityIds, amenityRepository, "amenityIds")
        val accessibilities = fetchAllByIds(request.accessibilityIds, accessibilityRepository, "accessibilityIds")
        val mealPlans = fetchAllByIds(request.mealPlanIds, mealPlanRepository, "mealPlanIds")
        val paymentTypes = fetchAllByIds(request.paymentTypeIds, paymentTypeRepository, "paymentTypeIds")
        val travelerExperiences = fetchAllByIds(
            request.travelerExperienceIds,
            travelerExperienceRepository,
            "travelerExperienceIds",
        )
        val address = Address(
            id = existingAddressId,
            streetAddress = request.address.streetAddress,
            extendedAddress = request.address.extendedAddress,
            city = request.address.city,
            stateProvince = request.address.stateProvince,
            postalCode = request.address.postalCode,
            countryCode = request.address.countryCode,
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
            travelerExperiences = travelerExperiences,
            location = request.location,
        )
    }

    private fun <T : Any> fetchAllByIds(
        ids: Set<Int>,
        repository: JpaRepository<T, Int>,
        fieldName: String,
    ): MutableSet<T> {
        if (ids.isEmpty()) return mutableSetOf()
        val entities = repository.findAllById(ids).toList()
        if (entities.size != ids.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName contains unknown ids")
        }
        return entities.toMutableSet()
    }

    private fun validateFilter(filter: StayFilter) {
        val hasCheckIn = filter.checkIn != null
        val hasCheckOut = filter.checkOut != null
        if (hasCheckIn && !hasCheckOut)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut is required when checkIn is provided")
        if (!hasCheckIn && hasCheckOut)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkIn is required when checkOut is provided")
        if (hasCheckIn && hasCheckOut && !filter.checkOut!!.isAfter(filter.checkIn))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn")
        filter.minPricePerNight?.let {
            if (it < BigDecimal.ZERO)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "minPricePerNight must not be negative")
        }
        filter.maxPricePerNight?.let {
            if (it < BigDecimal.ZERO)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxPricePerNight must not be negative")
        }
        if (filter.minPricePerNight != null && filter.maxPricePerNight != null
            && filter.minPricePerNight > filter.maxPricePerNight
        ) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "minPricePerNight must not exceed maxPricePerNight")
        filter.guests?.let {
            if (it < 1)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "guests must be at least 1")
        }
        filter.starRatings?.let { tiers ->
            if (tiers.any { it !in 1..5 })
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "starRatings must be between 1 and 5")
        }
        filter.bedrooms?.let { buckets ->
            if (buckets.any { it < 1 })
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bedrooms must be at least 1")
        }
        validateAmenityIds(filter.propertyAmenityIds, AmenityType.PROPERTY_AMENITY, "propertyAmenityIds")
        validateAmenityIds(filter.roomAmenityIds, AmenityType.ROOM_AMENITY, "roomAmenityIds")
    }

    private fun validateAmenityIds(ids: List<Int>?, expectedType: AmenityType, fieldName: String) {
        if (ids.isNullOrEmpty()) return
        ids.requireAllPositive(fieldName)
        val found = amenityRepository.findAllById(ids)
        if (found.size != ids.toSet().size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName contains unknown ids")
        }
        if (found.any { it.type != expectedType }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "$fieldName must reference ${expectedType.name} amenities",
            )
        }
    }

    private fun buildSpec(filter: StayFilter): Specification<Stay> =
        Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            if (filter.city != null || filter.countryCode != null) {
                val address = root.join<Stay, Address>("address", JoinType.INNER)
                filter.city?.let {
                    predicates += cb.like(cb.lower(address.get("city")), "%${it.lowercase()}%")
                }
                filter.countryCode?.let {
                    predicates += cb.equal(cb.lower(address.get<String>("countryCode")), it.lowercase())
                }
            }

            filter.propertyType?.let {
                predicates += cb.equal(root.get<Any>("propertyType"), it)
            }

            filter.maxPricePerNight?.let { max ->
                val sub = query!!.subquery(Int::class.java)
                val room = sub.from(Room::class.java)
                sub.select(cb.literal(1)).where(
                    cb.equal(room.get<Int>("stayId"), root.get<Int>("id")),
                    cb.le(room.get<BigDecimal>("price"), max),
                )
                predicates += cb.exists(sub)
            }

            filter.minPricePerNight?.let { min ->
                val sub = query!!.subquery(Int::class.java)
                val room = sub.from(Room::class.java)
                sub.select(cb.literal(1)).where(
                    cb.equal(room.get<Int>("stayId"), root.get<Int>("id")),
                    cb.ge(room.get<BigDecimal>("price"), min),
                )
                predicates += cb.exists(sub)
            }

            if (filter.checkIn != null && filter.checkOut != null) {
                val checkIn: LocalDate = filter.checkIn
                val checkOut: LocalDate = filter.checkOut
                val minSleeps = filter.guests ?: 1
                val sub = query!!.subquery(Int::class.java)
                val room = sub.from(Room::class.java)
                val conflictSub = sub.subquery(Int::class.java)
                val booking = conflictSub.from(Booking::class.java)
                val bookingRoom = booking.join<Booking, Room>("rooms")
                conflictSub.select(cb.literal(1)).where(
                    cb.equal(bookingRoom.get<Int>("id"), room.get<Int>("id")),
                    booking.get<BookingStatus>("status").`in`(
                        listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                    ),
                    cb.lessThan(booking.get("checkInDate"), checkOut),
                    cb.greaterThan(booking.get("checkOutDate"), checkIn),
                )
                sub.select(cb.literal(1)).where(
                    cb.equal(room.get<Int>("stayId"), root.get<Int>("id")),
                    cb.ge(room.get<Int>("sleeps"), minSleeps),
                    cb.not(cb.exists(conflictSub)),
                )
                predicates += cb.exists(sub)
            } else if (filter.guests != null) {
                val sub = query!!.subquery(Int::class.java)
                val room = sub.from(Room::class.java)
                sub.select(cb.literal(1)).where(
                    cb.equal(room.get<Int>("stayId"), root.get<Int>("id")),
                    cb.ge(room.get<Int>("sleeps"), filter.guests),
                )
                predicates += cb.exists(sub)
            }

            filter.starRatings?.takeIf { it.isNotEmpty() }?.let { tiers ->
                val tierPredicates = tiers.map { tier ->
                    cb.and(
                        cb.ge(root.get<BigDecimal>("starRating"), BigDecimal(tier) - BigDecimal("0.5")),
                        cb.lt(root.get<BigDecimal>("starRating"), BigDecimal(tier) + BigDecimal("0.5")),
                    )
                }
                predicates += cb.or(*tierPredicates.toTypedArray())
            }

            filter.bedrooms?.takeIf { it.isNotEmpty() }?.let { buckets ->
                val sub = query!!.subquery(Int::class.java)
                val room = sub.from(Room::class.java)
                val bucketPredicates = buckets.map { bucket ->
                    if (bucket >= 4) cb.ge(room.get<Int>("bedroomAmount"), 4)
                    else cb.equal(room.get<Int>("bedroomAmount"), bucket)
                }
                sub.select(cb.literal(1)).where(
                    cb.equal(room.get<Int>("stayId"), root.get<Int>("id")),
                    cb.or(*bucketPredicates.toTypedArray()),
                )
                predicates += cb.exists(sub)
            }

            filter.propertyAmenityIds?.takeIf { it.isNotEmpty() }?.let {
                predicates += hasAllAmenities(root, query!!, cb, it)
            }

            filter.roomAmenityIds?.takeIf { it.isNotEmpty() }?.let {
                predicates += hasAllAmenities(root, query!!, cb, it)
            }

            if (predicates.isEmpty()) null else cb.and(*predicates.toTypedArray())
        }

    private fun hasAllAmenities(
        root: Root<Stay>,
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder,
        amenityIds: List<Int>,
    ): Predicate {
        val sub = query.subquery(Long::class.java)
        val staySub = sub.from(Stay::class.java)
        val amenityJoin = staySub.join<Stay, Amenity>("amenities")
        sub.select(cb.countDistinct(amenityJoin.get<Int>("id"))).where(
            cb.equal(staySub.get<Int>("id"), root.get<Int>("id")),
            amenityJoin.get<Int>("id").`in`(amenityIds),
        )
        return cb.equal(sub, amenityIds.distinct().size.toLong())
    }
}
