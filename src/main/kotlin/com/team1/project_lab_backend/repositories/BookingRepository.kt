package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Booking
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookingRepository : JpaRepository<Booking, Int> {

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.user WHERE b.id IN :ids")
    fun findByIdInWithUser(ids: List<Int>): List<Booking>

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.rooms WHERE b.id IN :ids")
    fun findByIdInWithRooms(ids: List<Int>): List<Booking>
}
