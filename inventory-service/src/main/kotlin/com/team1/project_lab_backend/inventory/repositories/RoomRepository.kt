package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Room
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Int> {
    fun findByStayId(stayId: Int): List<Room>

    fun findByStayId(
        stayId: Int,
        pageable: Pageable,
    ): List<Room>

    fun findByStayIdIn(stayIds: Collection<Int>): List<Room>
}
