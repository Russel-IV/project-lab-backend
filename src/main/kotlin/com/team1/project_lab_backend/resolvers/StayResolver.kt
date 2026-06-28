package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.AddressRequest
import com.team1.project_lab_backend.dto.StayFilter
import com.team1.project_lab_backend.dto.StayRequest
import com.team1.project_lab_backend.models.PropertyType
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.services.StayService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal
import java.time.LocalDate

@Controller
class StayResolver(private val stayService: StayService) {

    @QueryMapping
    fun stays(
        @Argument filter: StayFilterInput?,
        @Argument page: Int?,
        @Argument size: Int?,
    ): List<Stay> = stayService.searchStays(filter?.toFilter() ?: StayFilter(), page ?: 0, size ?: 20)

    @QueryMapping
    fun stay(@Argument id: Int): Stay? = stayService.getStayById(id)

    @MutationMapping
    fun createStay(@Argument input: CreateStayInput): Stay {
        val currentUser = requireAuthenticated()
        return stayService.createStay(input.toRequest(), currentUser.id)
    }

    @MutationMapping
    fun updateStay(@Argument id: Int, @Argument input: UpdateStayInput): Stay {
        val currentUser = requireAuthenticated()
        return stayService.updateStay(id, input.toRequest(), currentUser.id)
    }

    @MutationMapping
    fun deleteStay(@Argument id: Int): Boolean {
        val currentUser = requireAuthenticated()
        stayService.deleteStay(id, currentUser.id)
        return true
    }
}

data class StayAddressInput(
    val streetAddress: String,
    val extendedAddress: String? = null,
    val city: String,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val countryCode: String,
)

data class CreateStayInput(
    val name: String,
    val about: String? = null,
    val propertyType: PropertyType,
    val address: StayAddressInput,
    val isRefundable: Boolean = false,
    val starRating: BigDecimal? = null,
    val daysFromBookingCancellationDeadline: Int? = null,
    val policiesText: String? = null,
    val importantInformation: String? = null,
    val hostId: Int,
    val propertyBrandId: Int? = null,
    val viewIds: Set<Int> = emptySet(),
    val amenityIds: Set<Int> = emptySet(),
    val accessibilityIds: Set<Int> = emptySet(),
    val mealPlanIds: Set<Int> = emptySet(),
    val paymentTypeIds: Set<Int> = emptySet(),
    val travelerExperienceIds: Set<Int> = emptySet(),
) {
    fun toRequest() = StayRequest(
        name = name,
        about = about,
        propertyType = propertyType,
        address = AddressRequest(
            streetAddress = address.streetAddress,
            extendedAddress = address.extendedAddress,
            city = address.city,
            stateProvince = address.stateProvince,
            postalCode = address.postalCode,
            countryCode = address.countryCode,
        ),
        isRefundable = isRefundable,
        starRating = starRating,
        daysFromBookingCancellationDeadline = daysFromBookingCancellationDeadline,
        policiesText = policiesText,
        importantInformation = importantInformation,
        hostId = hostId,
        propertyBrandId = propertyBrandId,
        viewIds = viewIds,
        amenityIds = amenityIds,
        accessibilityIds = accessibilityIds,
        mealPlanIds = mealPlanIds,
        paymentTypeIds = paymentTypeIds,
        travelerExperienceIds = travelerExperienceIds,
    )
}

data class UpdateStayInput(
    val name: String,
    val about: String? = null,
    val propertyType: PropertyType,
    val address: StayAddressInput,
    val isRefundable: Boolean = false,
    val starRating: BigDecimal? = null,
    val daysFromBookingCancellationDeadline: Int? = null,
    val policiesText: String? = null,
    val importantInformation: String? = null,
    val hostId: Int,
    val propertyBrandId: Int? = null,
    val viewIds: Set<Int> = emptySet(),
    val amenityIds: Set<Int> = emptySet(),
    val accessibilityIds: Set<Int> = emptySet(),
    val mealPlanIds: Set<Int> = emptySet(),
    val paymentTypeIds: Set<Int> = emptySet(),
    val travelerExperienceIds: Set<Int> = emptySet(),
) {
    fun toRequest() = StayRequest(
        name = name,
        about = about,
        propertyType = propertyType,
        address = AddressRequest(
            streetAddress = address.streetAddress,
            extendedAddress = address.extendedAddress,
            city = address.city,
            stateProvince = address.stateProvince,
            postalCode = address.postalCode,
            countryCode = address.countryCode,
        ),
        isRefundable = isRefundable,
        starRating = starRating,
        daysFromBookingCancellationDeadline = daysFromBookingCancellationDeadline,
        policiesText = policiesText,
        importantInformation = importantInformation,
        hostId = hostId,
        propertyBrandId = propertyBrandId,
        viewIds = viewIds,
        amenityIds = amenityIds,
        accessibilityIds = accessibilityIds,
        mealPlanIds = mealPlanIds,
        paymentTypeIds = paymentTypeIds,
        travelerExperienceIds = travelerExperienceIds,
    )
}

data class StayFilterInput(
    val city: String? = null,
    val countryCode: String? = null,
    val propertyType: PropertyType? = null,
    val minPricePerNight: BigDecimal? = null,
    val maxPricePerNight: BigDecimal? = null,
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guests: Int? = null,
) {
    fun toFilter() = StayFilter(
        city = city,
        countryCode = countryCode,
        propertyType = propertyType,
        minPricePerNight = minPricePerNight,
        maxPricePerNight = maxPricePerNight,
        checkIn = checkIn,
        checkOut = checkOut,
        guests = guests,
    )
}
