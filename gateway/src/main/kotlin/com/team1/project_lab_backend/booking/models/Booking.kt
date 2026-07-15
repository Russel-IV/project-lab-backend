package com.team1.project_lab_backend.booking.models

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.inventory.models.Room
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    open val user: User,

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
