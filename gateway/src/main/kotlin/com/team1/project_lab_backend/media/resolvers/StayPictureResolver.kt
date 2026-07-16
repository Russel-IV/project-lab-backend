package com.team1.project_lab_backend.media.resolvers

import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.media.services.StayPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class StayPictureResolver(private val stayPictureService: StayPictureService) {
    @QueryMapping
    fun stayPictures(
        @Argument stayId: Int,
    ): List<StayPicture> = stayPictureService.getPicturesForStayAsEntities(stayId)

    @MutationMapping
    fun updateStayPicture(
        @Argument stayId: Int,
        @Argument id: Int,
        @Argument input: UpdateStayPictureInput,
    ): StayPicture {
        val currentUser = requireAuthenticated()
        return stayPictureService.updatePictureMetadata(
            stayId = stayId,
            id = id,
            caption = input.caption,
            isPrimary = input.isPrimary,
            displayOrder = input.displayOrder,
            requestingUserId = currentUser.id,
        )
    }

    @MutationMapping
    fun deleteStayPicture(
        @Argument stayId: Int,
        @Argument id: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        stayPictureService.deletePicture(stayId, id, currentUser.id)
        return true
    }
}

data class UpdateStayPictureInput(
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
