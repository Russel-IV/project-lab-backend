package com.team1.project_lab_backend.inventory.resolvers.batch

import com.team1.project_lab_backend.inventory.models.Room
import com.team1.project_lab_backend.media.models.RoomPicture
import com.team1.project_lab_backend.media.repositories.RoomPictureRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class RoomBatchResolver(
    private val roomPictureRepository: RoomPictureRepository,
) {
    @BatchMapping
    fun pictures(rooms: List<Room>): Map<Room, List<RoomPicture>> {
        val ids = rooms.map { it.id }
        val byRoomId = roomPictureRepository.findByRoomIdIn(ids).groupBy { it.roomId }
        return rooms.associateWith { byRoomId[it.id] ?: emptyList() }
    }
}
