package com.team1.project_lab_backend.booking.models

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/** A mocked Stripe-style payment intent, priced and captured for a specific room/date/guest combination. */
@Entity
@Table(name = "payment_intent")
open class PaymentIntent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    @Column(name = "payment_intent_id", nullable = false, unique = true)
    open val paymentIntentId: String,
    @Column(name = "idempotency_key", nullable = false)
    open val idempotencyKey: String,
    @Column(name = "user_id", nullable = false)
    open val userId: Int,
    @Column(name = "check_in_date", nullable = false)
    open val checkInDate: LocalDate,
    @Column(name = "check_out_date", nullable = false)
    open val checkOutDate: LocalDate,
    @Column(name = "guests_count", nullable = false)
    open val guestsCount: Int,
    @Column(nullable = false)
    open val amount: Int,
    @Column(nullable = false)
    open val currency: String = "usd",
    @Column(name = "client_secret", nullable = false)
    open val clientSecret: String,
    // Set once createBooking consumes this intent; doubles as its idempotency marker.
    @Column(name = "booking_id")
    open val bookingId: Int? = null,
    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "payment_intent_room", joinColumns = [JoinColumn(name = "payment_intent_id")])
    @Column(name = "room_id")
    open val roomIds: MutableSet<Int> = mutableSetOf(),
)
