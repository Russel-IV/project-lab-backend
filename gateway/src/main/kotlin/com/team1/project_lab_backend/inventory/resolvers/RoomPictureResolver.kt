package com.team1.project_lab_backend.inventory.resolvers

import com.team1.project_lab_backend.inventory.services.RoomService
import com.team1.project_lab_backend.media.models.RoomPicture
import com.team1.project_lab_backend.media.services.RoomPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * Lives in inventory, not media, same rationale as StayPictureResolver: ownership
 * (room -> stay -> host) is an inventory-domain fact, checked here via RoomService
 * before delegating to media's (ownership-agnostic) RoomPictureService.
 */
@Controller
class RoomPictureResolver(
    private val roomPictureService: RoomPictureService,
    private val roomService: RoomService,
) {
    @QueryMapping
    suspend fun roomPictures(
        @Argument roomId: Int,
    ): List<RoomPicture> = roomPictureService.getPicturesForRoomAsEntities(roomId)

    @MutationMapping
    suspend fun updateRoomPicture(
        @Argument roomId: Int,
        @Argument id: Int,
        @Argument input: UpdateRoomPictureInput,
    ): RoomPicture {
        val currentUser = requireAuthenticated()
        roomService.requireOwnedByHost(roomId, currentUser.id)
        return roomPictureService.updatePictureMetadata(
            roomId = roomId,
            id = id,
            caption = input.caption,
            isPrimary = input.isPrimary,
            displayOrder = input.displayOrder,
        )
    }

    @MutationMapping
    suspend fun deleteRoomPicture(
        @Argument roomId: Int,
        @Argument id: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        roomService.requireOwnedByHost(roomId, currentUser.id)
        roomPictureService.deletePicture(roomId, id)
        return true
    }
}

data class UpdateRoomPictureInput(
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
