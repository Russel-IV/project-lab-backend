package com.team1.project_lab_backend.booking.models

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}

@Entity
@Table(name = "booking")
open class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    // User lives in identity-service (docs/adr/0002, docs/adr/0011) — no FK, no live
    // JPA relation, and no existence check either: userId is always the
    // JWT-authenticated caller's own id (set by the Gateway's BookingResolver before
    // this service ever sees the request), and a valid JWT already implies a real user.
    @Column(name = "user_id", nullable = false)
    open val userId: Int,
    @Column(name = "check_in_date", nullable = false)
    open val checkInDate: LocalDate,
    @Column(name = "check_out_date", nullable = false)
    open val checkOutDate: LocalDate,
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    open val status: BookingStatus = BookingStatus.PENDING,
    @Column(name = "guests_count", nullable = false)
    open val guestsCount: Int,
    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    open val totalPrice: BigDecimal = BigDecimal.ZERO,
    // Room lives in inventory-service (docs/adr/0002, docs/adr/0010) — no FK, no live
    // JPA relation to a Room entity. The booking_room bridge table is this service's
    // own data, just mapped as a plain id collection instead of an object graph.
    // RoomFeignClient resolves these ids to full Room details when needed (createBooking
    // validation, hasCompletedBookingForStay).
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "booking_room", joinColumns = [JoinColumn(name = "booking_id")])
    @Column(name = "room_id")
    open val roomIds: MutableSet<Int> = mutableSetOf(),
)
