package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.StayPicture
import org.springframework.data.jpa.repository.JpaRepository

interface StayPictureRepository : JpaRepository<StayPicture, Int> {

    fun findByStayId(stayId: Int): List<StayPicture>

    fun findByStayIdIn(stayIds: Collection<Int>): List<StayPicture>

    fun findByStayIdAndId(stayId: Int, id: Int): StayPicture?

    fun existsByStayIdAndIsPrimary(stayId: Int, isPrimary: Boolean): Boolean
}
