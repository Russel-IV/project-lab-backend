package com.team1.project_lab_backend.inventory.resolvers

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.models.AmenityType
import com.team1.project_lab_backend.inventory.models.Destination
import com.team1.project_lab_backend.inventory.models.MealPlan
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import com.team1.project_lab_backend.inventory.models.View
import com.team1.project_lab_backend.inventory.services.AccessibilityService
import com.team1.project_lab_backend.inventory.services.AmenityService
import com.team1.project_lab_backend.inventory.services.DestinationService
import com.team1.project_lab_backend.inventory.services.MealPlanService
import com.team1.project_lab_backend.inventory.services.PaymentTypeService
import com.team1.project_lab_backend.inventory.services.PropertyBrandService
import com.team1.project_lab_backend.inventory.services.TravelerExperienceService
import com.team1.project_lab_backend.inventory.services.ViewService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class LookupResolver(
    private val amenityService: AmenityService,
    private val accessibilityService: AccessibilityService,
    private val viewService: ViewService,
    private val paymentTypeService: PaymentTypeService,
    private val mealPlanService: MealPlanService,
    private val propertyBrandService: PropertyBrandService,
    private val travelerExperienceService: TravelerExperienceService,
    private val destinationService: DestinationService,
) {
    // ---- Amenity ----

    @QueryMapping
    suspend fun amenities(): List<Amenity> = amenityService.getAllAmenities()

    @QueryMapping
    suspend fun amenity(
        @Argument id: Int,
    ): Amenity = amenityService.getAmenityById(id)

    @MutationMapping
    suspend fun createAmenity(
        @Argument input: CreateAmenityInput,
    ): Amenity {
        requireAuthenticated()
        return amenityService.createAmenity(AmenityRequest(name = input.name, type = input.type))
    }

    @MutationMapping
    suspend fun updateAmenity(
        @Argument id: Int,
        @Argument input: UpdateAmenityInput,
    ): Amenity {
        requireAuthenticated()
        return amenityService.updateAmenity(id, AmenityRequest(name = input.name, type = input.type))
    }

    @MutationMapping
    suspend fun deleteAmenity(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        amenityService.deleteAmenity(id)
        return true
    }

    // ---- Accessibility ----

    @QueryMapping
    suspend fun accessibilities(): List<Accessibility> = accessibilityService.getAllAccessibility()

    @MutationMapping
    suspend fun createAccessibility(
        @Argument input: CreateAccessibilityInput,
    ): Accessibility {
        requireAuthenticated()
        return accessibilityService.createAccessibility(AccessibilityRequest(accessibilityType = input.accessibilityType))
    }

    @MutationMapping
    suspend fun updateAccessibility(
        @Argument id: Int,
        @Argument input: UpdateAccessibilityInput,
    ): Accessibility {
        requireAuthenticated()
        return accessibilityService.updateAccessibility(id, AccessibilityRequest(accessibilityType = input.accessibilityType))
    }

    @MutationMapping
    suspend fun deleteAccessibility(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        accessibilityService.deleteAccessibility(id)
        return true
    }

    // ---- View ----

    @QueryMapping
    suspend fun views(): List<View> = viewService.getAllViews()

    @MutationMapping
    suspend fun createView(
        @Argument input: CreateViewInput,
    ): View {
        requireAuthenticated()
        return viewService.createView(ViewRequest(viewType = input.viewType))
    }

    @MutationMapping
    suspend fun updateView(
        @Argument id: Int,
        @Argument input: UpdateViewInput,
    ): View {
        requireAuthenticated()
        return viewService.updateView(id, ViewRequest(viewType = input.viewType))
    }

    @MutationMapping
    suspend fun deleteView(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        viewService.deleteView(id)
        return true
    }

    // ---- PaymentType ----

    @QueryMapping
    suspend fun paymentTypes(): List<PaymentType> = paymentTypeService.getAllPaymentTypes()

    @MutationMapping
    suspend fun createPaymentType(
        @Argument input: CreatePaymentTypeInput,
    ): PaymentType {
        requireAuthenticated()
        return paymentTypeService.createPaymentType(PaymentTypeRequest(paymentType = input.paymentType))
    }

    @MutationMapping
    suspend fun updatePaymentType(
        @Argument id: Int,
        @Argument input: UpdatePaymentTypeInput,
    ): PaymentType {
        requireAuthenticated()
        return paymentTypeService.updatePaymentType(id, PaymentTypeRequest(paymentType = input.paymentType))
    }

    @MutationMapping
    suspend fun deletePaymentType(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        paymentTypeService.deletePaymentType(id)
        return true
    }

    // ---- MealPlan ----

    @QueryMapping
    suspend fun mealPlans(): List<MealPlan> = mealPlanService.getAllMealPlans()

    @MutationMapping
    suspend fun createMealPlan(
        @Argument input: CreateMealPlanInput,
    ): MealPlan {
        requireAuthenticated()
        return mealPlanService.createMealPlan(MealPlanRequest(mealPlanType = input.mealPlanType))
    }

    @MutationMapping
    suspend fun updateMealPlan(
        @Argument id: Int,
        @Argument input: UpdateMealPlanInput,
    ): MealPlan {
        requireAuthenticated()
        return mealPlanService.updateMealPlan(id, MealPlanRequest(mealPlanType = input.mealPlanType))
    }

    @MutationMapping
    suspend fun deleteMealPlan(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        mealPlanService.deleteMealPlan(id)
        return true
    }

    // ---- PropertyBrand ----

    @QueryMapping
    suspend fun propertyBrands(): List<PropertyBrand> = propertyBrandService.getAllPropertyBrands()

    @QueryMapping
    suspend fun propertyBrand(
        @Argument id: Int,
    ): PropertyBrand = propertyBrandService.getPropertyBrandById(id)

    @MutationMapping
    suspend fun createPropertyBrand(
        @Argument input: CreatePropertyBrandInput,
    ): PropertyBrand {
        requireAuthenticated()
        return propertyBrandService.createPropertyBrand(PropertyBrandRequest(brandName = input.brandName))
    }

    @MutationMapping
    suspend fun updatePropertyBrand(
        @Argument id: Int,
        @Argument input: UpdatePropertyBrandInput,
    ): PropertyBrand {
        requireAuthenticated()
        return propertyBrandService.updatePropertyBrand(id, PropertyBrandRequest(brandName = input.brandName))
    }

    @MutationMapping
    suspend fun deletePropertyBrand(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        propertyBrandService.deletePropertyBrand(id)
        return true
    }

    // ---- TravelerExperience ----

    @QueryMapping
    suspend fun travelerExperiences(): List<TravelerExperience> = travelerExperienceService.getAllTravelerExperiences()

    @MutationMapping
    suspend fun createTravelerExperience(
        @Argument input: CreateTravelerExperienceInput,
    ): TravelerExperience {
        requireAuthenticated()
        return travelerExperienceService.createTravelerExperience(
            TravelerExperienceRequest(travelerExperienceType = input.travelerExperienceType),
        )
    }

    @MutationMapping
    suspend fun updateTravelerExperience(
        @Argument id: Int,
        @Argument input: UpdateTravelerExperienceInput,
    ): TravelerExperience {
        requireAuthenticated()
        return travelerExperienceService.updateTravelerExperience(
            id,
            TravelerExperienceRequest(travelerExperienceType = input.travelerExperienceType),
        )
    }

    @MutationMapping
    suspend fun deleteTravelerExperience(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        travelerExperienceService.deleteTravelerExperience(id)
        return true
    }

    // ---- Destination ----

    @QueryMapping
    suspend fun destinations(
        @Argument search: String?,
        @Argument limit: Int?,
    ): List<Destination> = destinationService.searchDestinations(search, limit ?: 20)

    @QueryMapping
    suspend fun popularDestinations(
        @Argument limit: Int?,
    ): List<Destination> = destinationService.popularDestinations(limit ?: 8)
}

data class CreateAmenityInput(val name: String, val type: AmenityType)

data class UpdateAmenityInput(val name: String, val type: AmenityType)

data class CreateAccessibilityInput(val accessibilityType: String)

data class UpdateAccessibilityInput(val accessibilityType: String)

data class CreateViewInput(val viewType: String)

data class UpdateViewInput(val viewType: String)

data class CreatePaymentTypeInput(val paymentType: String)

data class UpdatePaymentTypeInput(val paymentType: String)

data class CreateMealPlanInput(val mealPlanType: String)

data class UpdateMealPlanInput(val mealPlanType: String)

data class CreatePropertyBrandInput(val brandName: String)

data class UpdatePropertyBrandInput(val brandName: String)

data class CreateTravelerExperienceInput(val travelerExperienceType: String)

data class UpdateTravelerExperienceInput(val travelerExperienceType: String)
