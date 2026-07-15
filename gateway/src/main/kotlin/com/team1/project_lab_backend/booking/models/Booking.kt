package com.team1.project_lab_backend.booking.models

import com.team1.project_lab_backend.inventory.models.Room
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

@Entity
@Table(name = "booking")
open class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    // User lives in identity-service now (docs/adr/0002, docs/adr/0011, Phase 4) — no
    // FK, no live JPA relation, and no existence check either: userId is always the
    // JWT-authenticated caller's own id (BookingResolver.createBooking), and a valid
    // JWT already implies a real user. BookingBatchResolver.user() Feign-fetches user
    // details for the GraphQL Booking.user field.
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "booking_room",
        joinColumns = [JoinColumn(name = "booking_id")],
        inverseJoinColumns = [JoinColumn(name = "room_id")]
    )
    open val rooms: MutableSet<Room> = mutableSetOf()
)
