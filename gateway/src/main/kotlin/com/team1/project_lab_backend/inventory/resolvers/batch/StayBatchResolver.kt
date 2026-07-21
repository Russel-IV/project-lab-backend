package com.team1.project_lab_backend.inventory.resolvers.batch

import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.services.HostFeignClient
import com.team1.project_lab_backend.inventory.models.Accessibility
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.Amenity
import com.team1.project_lab_backend.inventory.models.MealPlan
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import com.team1.project_lab_backend.inventory.models.View
import com.team1.project_lab_backend.inventory.services.AccessibilityFeignClient
import com.team1.project_lab_backend.inventory.services.AmenityFeignClient
import com.team1.project_lab_backend.inventory.services.MealPlanFeignClient
import com.team1.project_lab_backend.inventory.services.PaymentTypeFeignClient
import com.team1.project_lab_backend.inventory.services.PropertyBrandFeignClient
import com.team1.project_lab_backend.inventory.services.RoomFeignClient
import com.team1.project_lab_backend.inventory.services.TravelerExperienceFeignClient
import com.team1.project_lab_backend.inventory.services.ViewFeignClient
import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.media.services.StayPictureService
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

/**
 * Every field here used to be a local JPA fetch-join; Stay is now a flat Feign-fetched
 * DTO that only carries ids for its relations (docs/adr/0002, docs/adr/0010, Phase 5),
 * so each of these becomes a bulk call to inventory-service, same pattern as host()
 * already used since Phase 4.
 *
 * docs/adr/0025: each method below makes exactly one downstream call, so there's no
 * manual coroutineScope/awaitAll needed inside any single method — the fan-out win is
 * that Spring for GraphQL's reactive engine can now schedule these 10 sibling
 * @BatchMapping methods concurrently against each other (each is a suspend fun,
 * bridged to a Mono), instead of running each to completion sequentially the way a
 * plain synchronous DataFetcher forces it to under the servlet stack.
 */
@Controller
class StayBatchResolver(
    private val roomFeignClient: RoomFeignClient,
    private val stayPictureService: StayPictureService,
    private val hostFeignClient: HostFeignClient,
    private val propertyBrandFeignClient: PropertyBrandFeignClient,
    private val amenityFeignClient: AmenityFeignClient,
    private val viewFeignClient: ViewFeignClient,
    private val accessibilityFeignClient: AccessibilityFeignClient,
    private val mealPlanFeignClient: MealPlanFeignClient,
    private val paymentTypeFeignClient: PaymentTypeFeignClient,
    private val travelerExperienceFeignClient: TravelerExperienceFeignClient,
) {
    @BatchMapping
    suspend fun rooms(stays: List<Stay>): Map<Stay, List<Room>> {
        val ids = stays.map { it.id }
        val byStayId = roomFeignClient.list(ids = null, stayId = null, stayIds = ids, page = 0, size = 0).groupBy { it.stayId }
        return stays.associateWith { byStayId[it.id] ?: emptyList() }
    }

    @BatchMapping
    suspend fun pictures(stays: List<Stay>): Map<Stay, List<StayPicture>> {
        val ids = stays.map { it.id }
        val byStayId = stayPictureService.getPicturesForStays(ids)
        return stays.associateWith { byStayId[it.id] ?: emptyList() }
    }

    @BatchMapping
    suspend fun host(stays: List<Stay>): Map<Stay, Host> {
        val ids = stays.map { it.hostId }.distinct()
        val loaded = hostFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.hostId]!! }
    }

    @BatchMapping
    suspend fun propertyBrand(stays: List<Stay>): Map<Stay, PropertyBrand?> {
        val ids = stays.mapNotNull { it.propertyBrandId }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else propertyBrandFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { it.propertyBrandId?.let { id -> loaded[id] } }
    }

    @BatchMapping
    suspend fun amenities(stays: List<Stay>): Map<Stay, List<Amenity>> {
        val ids = stays.flatMap { it.amenityIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else amenityFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.amenityIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun views(stays: List<Stay>): Map<Stay, List<View>> {
        val ids = stays.flatMap { it.viewIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else viewFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.viewIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun accessibilities(stays: List<Stay>): Map<Stay, List<Accessibility>> {
        val ids = stays.flatMap { it.accessibilityIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else accessibilityFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.accessibilityIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun mealPlans(stays: List<Stay>): Map<Stay, List<MealPlan>> {
        val ids = stays.flatMap { it.mealPlanIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else mealPlanFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.mealPlanIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun paymentTypes(stays: List<Stay>): Map<Stay, List<PaymentType>> {
        val ids = stays.flatMap { it.paymentTypeIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else paymentTypeFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.paymentTypeIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun travelerExperiences(stays: List<Stay>): Map<Stay, List<TravelerExperience>> {
        val ids = stays.flatMap { it.travelerExperienceIds }.distinct()
        val loaded = if (ids.isEmpty()) emptyMap() else travelerExperienceFeignClient.list(ids).associateBy { it.id }
        return stays.associateWith { stay -> stay.travelerExperienceIds.mapNotNull { loaded[it] } }
    }

    @BatchMapping
    suspend fun startingFromPrice(stays: List<Stay>): Map<Stay, BigDecimal?> {
        val ids = stays.map { it.id }
        val byStayId = roomFeignClient.list(ids = null, stayId = null, stayIds = ids, page = 0, size = 0).groupBy { it.stayId }
        return stays.associateWith { byStayId[it.id]?.map { r -> r.price }?.minOrNull() }
    }

    @BatchMapping
    fun address(stays: List<Stay>): Map<Stay, Address> = stays.associateWith { it.address }
}
