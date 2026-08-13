package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Room
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal

interface RoomRepository : JpaRepository<Room, Int> {
    fun findByStayId(stayId: Int): List<Room>

    fun findByStayId(
        stayId: Int,
        pageable: Pageable,
    ): List<Room>

    fun findByStayIdIn(stayIds: Collection<Int>): List<Room>

    @Query("SELECT MIN(r.price) FROM Room r WHERE r.stayId IN :stayIds GROUP BY r.stayId")
    fun findMinPricePerStay(stayIds: Collection<Int>): List<BigDecimal>
}
