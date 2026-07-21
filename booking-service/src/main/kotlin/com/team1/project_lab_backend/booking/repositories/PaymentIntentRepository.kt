package com.team1.project_lab_backend.booking.repositories

import com.team1.project_lab_backend.booking.models.PaymentIntent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentIntentRepository : JpaRepository<PaymentIntent, Int> {
    fun findByPaymentIntentId(paymentIntentId: String): Optional<PaymentIntent>

    fun findByUserIdAndIdempotencyKey(
        userId: Int,
        idempotencyKey: String,
    ): Optional<PaymentIntent>
}
