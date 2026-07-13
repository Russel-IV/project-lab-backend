package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.PaymentMethod
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentMethodRepository : JpaRepository<PaymentMethod, Int> {

    fun findByUserId(userId: Int): List<PaymentMethod>
    fun findByIdAndUserId(id: Int, userId: Int): PaymentMethod?
}
