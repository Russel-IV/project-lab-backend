package com.team1.project_lab_backend.identity.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payment_method")
data class PaymentMethod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Int,
    @Column(name = "stripe_payment_method_id", nullable = false, columnDefinition = "TEXT")
    val stripePaymentMethodId: String,
    @Column(name = "brand", nullable = false, length = 32)
    val brand: String,
    @Column(name = "last_four", nullable = false, length = 4)
    val lastFour: String,
    @Column(name = "type", nullable = false, length = 32)
    val type: String,
    @Column(name = "expiry_month", nullable = false)
    val expiryMonth: Int,
    @Column(name = "expiry_year", nullable = false)
    val expiryYear: Int,
    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false,
)
