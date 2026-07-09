package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.BookingStatus
import com.team1.project_lab_backend.models.Room
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface RoomRepository : JpaRepository<Room, Int> {

    fun findByStayId(stayId: Int): List<Room>

    fun findByStayId(stayId: Int, pageable: Pageable): List<Room>

    fun findByStayIdIn(stayIds: Collection<Int>): List<Room>

    @Query("""
        SELECT r FROM Room r
        WHERE r.stayId = :stayId
        AND (:guests IS NULL OR r.sleeps >= :guests)
        AND NOT EXISTS (
            SELECT 1 FROM Booking b JOIN b.rooms r2
            WHERE r2.id = r.id
            AND b.status IN :activeStatuses
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
        )
    """)
    fun findAvailableRooms(
        stayId: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        activeStatuses: List<BookingStatus>,
        guests: Int? = null
    ): List<Room>

    @Query("""
        SELECT r FROM Room r
        WHERE r.id IN :roomIds
        AND EXISTS (
            SELECT 1 FROM Booking b JOIN b.rooms r2
            WHERE r2.id = r.id
            AND b.status IN :activeStatuses
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
        )
    """)
    fun findConflictingRooms(
        roomIds: Set<Int>,
        checkIn: LocalDate,
        checkOut: LocalDate,
        activeStatuses: List<BookingStatus>
    ): List<Room>
}
