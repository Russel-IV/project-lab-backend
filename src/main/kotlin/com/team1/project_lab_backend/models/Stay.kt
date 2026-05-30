package com.team1.project_lab_backend.models

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

enum class PropertyType {
    HOTEL, HOME
}

@Entity
@Table(name = "stay")
open class Stay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @field:NotNull
    @field:DecimalMin("0.00")
    open val price: BigDecimal,

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    @field:NotBlank
    open val name: String,

    @Column(name = "about", columnDefinition = "TEXT")
    open val about: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    open val propertyType: PropertyType? = null,

    @Column(name = "street_address", nullable = false, columnDefinition = "TEXT")
    @field:NotBlank
    open val streetAddress: String,

    @Column(name = "extended_address", columnDefinition = "TEXT")
    open val extendedAddress: String? = null,

    @Column(name = "city", nullable = false, columnDefinition = "TEXT")
    @field:NotBlank
    open val city: String,

    @Column(name = "state_province", columnDefinition = "TEXT")
    open val stateProvince: String? = null,

    @Column(name = "postal_code", columnDefinition = "TEXT")
    open val postalCode: String? = null,

    @Column(name = "country_code", columnDefinition = "TEXT")
    open val countryCode: String? = null,

    @Column(name = "is_available", nullable = false)
    open val isAvailable: Boolean = true,

    @Column(name = "is_refundable", nullable = false)
    open val isRefundable: Boolean = false,

    @Column(name = "star_rating", precision = 3, scale = 1)
    @field:DecimalMin("0.0")
    @field:DecimalMax("5.0")
    open val starRating: BigDecimal? = null,

    @Column(name = "sleeps", nullable = false)
    @field:Min(1)
    open val sleeps: Int,

    @Column(name = "bedroom_amount", nullable = false)
    @field:Min(0)
    open val bedroomAmount: Int,

    @Column(name = "bathrooms", nullable = false, precision = 3, scale = 1)
    @field:NotNull
    @field:DecimalMin("0.0")
    open val bathrooms: BigDecimal,

    @Column(name = "size", precision = 10, scale = 1)
    @field:DecimalMin("0.0")
    open val size: BigDecimal? = null,

    @Column(name = "days_from_booking_cancellation_deadline")
    @field:Min(0)
    open val daysFromBookingCancellationDeadline: Int? = null,

    @Column(name = "policies_text", columnDefinition = "TEXT")
    open val policiesText: String? = null,

    @Column(name = "important_information", columnDefinition = "TEXT")
    open val importantInformation: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    open val host: Host,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_brand_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    open val propertyBrand: PropertyBrand? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_view",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "view_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val views: MutableSet<View> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_amenity",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "amenity_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val amenities: MutableSet<Amenity> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_accessibility",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "accessibility_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val accessibilities: MutableSet<Accessibility> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_meal_plan",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "meal_plan_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val mealPlans: MutableSet<MealPlan> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_payment_type",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "payment_type_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val paymentTypes: MutableSet<PaymentType> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stay_traveler_experience",
        joinColumns = [JoinColumn(name = "stay_id")],
        inverseJoinColumns = [JoinColumn(name = "traveler_experience_id")]
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val travelerExperiences: MutableSet<TravelerExperience> = mutableSetOf()
)
