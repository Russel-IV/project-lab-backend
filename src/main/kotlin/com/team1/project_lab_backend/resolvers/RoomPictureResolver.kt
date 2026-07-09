package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.models.RoomPicture
import com.team1.project_lab_backend.services.RoomPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class RoomPictureResolver(private val roomPictureService: RoomPictureService) {

    @SchemaMapping(typeName = "RoomPicture", field = "url")
    fun url(roomPicture: RoomPicture): String = roomPictureService.resolveUrl(roomPicture)

    @QueryMapping
    fun roomPictures(@Argument roomId: Int): List<RoomPicture> =
        roomPictureService.getPicturesForRoomAsEntities(roomId)

    @MutationMapping
    fun updateRoomPicture(
        @Argument roomId: Int,
        @Argument id: Int,
        @Argument input: UpdateRoomPictureInput,
    ): RoomPicture {
        val currentUser = requireAuthenticated()
        return roomPictureService.updatePictureMetadata(
            roomId = roomId,
            id = id,
            caption = input.caption,
            isPrimary = input.isPrimary,
            displayOrder = input.displayOrder,
            requestingUserId = currentUser.id,
        )
    }

    @MutationMapping
    fun deleteRoomPicture(@Argument roomId: Int, @Argument id: Int): Boolean {
        val currentUser = requireAuthenticated()
        roomPictureService.deletePicture(roomId, id, currentUser.id)
        return true
    }
}

data class UpdateRoomPictureInput(
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
