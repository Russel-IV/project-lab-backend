package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.RoomPicture
import org.springframework.data.jpa.repository.JpaRepository

interface RoomPictureRepository : JpaRepository<RoomPicture, Int> {

    fun findByRoomId(roomId: Int): List<RoomPicture>

    fun findByRoomIdIn(roomIds: Collection<Int>): List<RoomPicture>

    fun findByRoomIdAndId(roomId: Int, id: Int): RoomPicture?

    fun existsByRoomIdAndIsPrimary(roomId: Int, isPrimary: Boolean): Boolean
}
