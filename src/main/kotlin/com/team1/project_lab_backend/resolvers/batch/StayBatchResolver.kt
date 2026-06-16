package com.team1.project_lab_backend.resolvers.batch

import com.team1.project_lab_backend.models.Amenity
import com.team1.project_lab_backend.models.Accessibility
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.MealPlan
import com.team1.project_lab_backend.models.PaymentType
import com.team1.project_lab_backend.models.PropertyBrand
import com.team1.project_lab_backend.models.Room
import com.team1.project_lab_backend.models.Stay
import com.team1.project_lab_backend.models.StayPicture
import com.team1.project_lab_backend.models.TravelerExperience
import com.team1.project_lab_backend.models.View
import com.team1.project_lab_backend.repositories.RoomRepository
import com.team1.project_lab_backend.repositories.StayPictureRepository
import com.team1.project_lab_backend.repositories.StayRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class StayBatchResolver(
    private val stayRepository: StayRepository,
    private val roomRepository: RoomRepository,
    private val stayPictureRepository: StayPictureRepository,
) {
    @BatchMapping
    fun rooms(stays: List<Stay>): Map<Stay, List<Room>> {
        val ids = stays.map { it.id }
        val byStayId = roomRepository.findByStayIdIn(ids).groupBy { it.stayId }
        return stays.associateWith { byStayId[it.id] ?: emptyList() }
    }

    @BatchMapping
    fun pictures(stays: List<Stay>): Map<Stay, List<StayPicture>> {
        val ids = stays.map { it.id }
        val byStayId = stayPictureRepository.findByStayIdIn(ids).groupBy { it.stayId }
        return stays.associateWith { byStayId[it.id] ?: emptyList() }
    }

    @BatchMapping
    fun host(stays: List<Stay>): Map<Stay, Host> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithHost(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]!!.host }
    }

    @BatchMapping
    fun propertyBrand(stays: List<Stay>): Map<Stay, PropertyBrand?> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithPropertyBrand(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.propertyBrand }
    }

    @BatchMapping
    fun amenities(stays: List<Stay>): Map<Stay, List<Amenity>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithAmenities(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.amenities?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun views(stays: List<Stay>): Map<Stay, List<View>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithViews(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.views?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun accessibilities(stays: List<Stay>): Map<Stay, List<Accessibility>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithAccessibilities(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.accessibilities?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun mealPlans(stays: List<Stay>): Map<Stay, List<MealPlan>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithMealPlans(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.mealPlans?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun paymentTypes(stays: List<Stay>): Map<Stay, List<PaymentType>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithPaymentTypes(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.paymentTypes?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun travelerExperiences(stays: List<Stay>): Map<Stay, List<TravelerExperience>> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithTravelerExperiences(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]?.travelerExperiences?.toList() ?: emptyList() }
    }

    @BatchMapping
    fun startingFromPrice(stays: List<Stay>): Map<Stay, BigDecimal?> {
        val ids = stays.map { it.id }
        val byStayId = roomRepository.findByStayIdIn(ids).groupBy { it.stayId }
        return stays.associateWith { byStayId[it.id]?.map { r -> r.price }?.minOrNull() }
    }

    @BatchMapping
    fun address(stays: List<Stay>): Map<Stay, com.team1.project_lab_backend.models.Address> {
        val ids = stays.map { it.id }
        val loaded = stayRepository.findByIdInWithAddress(ids).associateBy { it.id }
        return stays.associateWith { loaded[it.id]!!.address }
    }
}
