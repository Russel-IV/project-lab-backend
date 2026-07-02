package com.team1.project_lab_backend.models

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.locationtech.jts.geom.Point

enum class PropertyType {
    HOTEL, HOME
}

@Entity
@Table(name = "stay")
open class Stay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    @field:NotBlank
    open val name: String,

    @Column(name = "about", columnDefinition = "TEXT")
    open val about: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    open val propertyType: PropertyType,

    @Column(name = "is_refundable", nullable = false)
    open val isRefundable: Boolean = false,

    @Column(name = "star_rating", precision = 3, scale = 1)
    @field:DecimalMin("0.0")
    @field:DecimalMax("5.0")
    open val starRating: BigDecimal? = null,

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

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    open val address: Address,

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
    open val travelerExperiences: MutableSet<TravelerExperience> = mutableSetOf(),

    @Column(columnDefinition = "geography(Point, 4326)")
    open val location: Point? = null,
)
