package com.team1.project_lab_backend.booking.repositories

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookingRepository : JpaRepository<Booking, Int> {

    fun findByUserId(userId: Int, pageable: Pageable): List<Booking>

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.rooms WHERE b.id IN :ids")
    fun findByIdInWithRooms(ids: List<Int>): List<Booking>

    @Query(
        "SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b JOIN b.rooms r " +
            "WHERE b.userId = :userId AND r.stayId = :stayId AND b.status = :status",
    )
    fun existsBookingForUserAndStayWithStatus(userId: Int, stayId: Int, status: BookingStatus): Boolean
}
