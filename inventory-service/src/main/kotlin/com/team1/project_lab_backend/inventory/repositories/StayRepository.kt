package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Stay
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface StayRepository : JpaRepository<Stay, Int>, JpaSpecificationExecutor<Stay> {
    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.propertyBrand WHERE s.id IN :ids")
    fun findByIdInWithPropertyBrand(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.address WHERE s.id IN :ids")
    fun findByIdInWithAddress(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.amenities WHERE s.id IN :ids")
    fun findByIdInWithAmenities(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.views WHERE s.id IN :ids")
    fun findByIdInWithViews(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.accessibilities WHERE s.id IN :ids")
    fun findByIdInWithAccessibilities(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.mealPlans WHERE s.id IN :ids")
    fun findByIdInWithMealPlans(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.paymentTypes WHERE s.id IN :ids")
    fun findByIdInWithPaymentTypes(ids: List<Int>): List<Stay>

    @Query("SELECT DISTINCT s FROM Stay s LEFT JOIN FETCH s.travelerExperiences WHERE s.id IN :ids")
    fun findByIdInWithTravelerExperiences(ids: List<Int>): List<Stay>
}
