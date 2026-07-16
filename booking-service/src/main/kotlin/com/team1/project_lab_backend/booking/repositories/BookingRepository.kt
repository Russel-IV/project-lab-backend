package com.team1.project_lab_backend.booking.repositories

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface BookingRepository : JpaRepository<Booking, Int> {
    fun findByUserId(
        userId: Int,
        pageable: Pageable,
    ): List<Booking>

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.roomIds WHERE b.id IN :ids")
    fun findByIdInWithRoomIds(ids: List<Int>): List<Booking>

    // Pure booking_room + booking data — only room ids (not any Room attribute) are
    // required to answer "which of these rooms conflict with this date range".
    // Backs both this service's own createBooking check and the
    // /internal/bookings/conflicting-room-ids endpoint inventory-service calls
    // (BookingConflictController).
    @Query(
        """
        SELECT DISTINCT ri FROM Booking b JOIN b.roomIds ri
        WHERE (:roomIds IS NULL OR ri IN :roomIds)
        AND b.status IN :activeStatuses
        AND b.checkInDate < :checkOut
        AND b.checkOutDate > :checkIn
        """,
    )
    fun findConflictingRoomIds(
        roomIds: List<Int>?,
        checkIn: LocalDate,
        checkOut: LocalDate,
        activeStatuses: List<BookingStatus>,
    ): Set<Int>

    // Used by hasCompletedBookingForStay — returns every room id the user has a
    // booking with the given status against, so the caller can resolve those ids to
    // stayIds via inventory-service and check for a match. Room's own stayId column
    // isn't reachable from here.
    @Query("SELECT DISTINCT ri FROM Booking b JOIN b.roomIds ri WHERE b.userId = :userId AND b.status = :status")
    fun findRoomIdsForUserWithStatus(
        userId: Int,
        status: BookingStatus,
    ): Set<Int>
}
