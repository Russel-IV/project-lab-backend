package com.team1.project_lab_backend.inventory.resolvers

import com.team1.project_lab_backend.inventory.services.StayService
import com.team1.project_lab_backend.media.models.StayPicture
import com.team1.project_lab_backend.media.services.StayPictureService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * Lives in inventory, not media, even though it's the picture CRUD entry point:
 * ownership (stay.hostId == requestingUserId) is an inventory-domain fact media-service
 * has no way to check itself, so this resolver verifies it via StayService before
 * delegating to media's (ownership-agnostic) StayPictureService — see
 * ModularityTests.kt for why this split exists.
 */
@Controller
class StayPictureResolver(
    private val stayPictureService: StayPictureService,
    private val stayService: StayService,
) {
    @QueryMapping
    suspend fun stayPictures(
        @Argument stayId: Int,
    ): List<StayPicture> = stayPictureService.getPicturesForStayAsEntities(stayId)

    @MutationMapping
    suspend fun updateStayPicture(
        @Argument stayId: Int,
        @Argument id: Int,
        @Argument input: UpdateStayPictureInput,
    ): StayPicture {
        val currentUser = requireAuthenticated()
        stayService.requireOwnedByHost(stayId, currentUser.id)
        return stayPictureService.updatePictureMetadata(
            stayId = stayId,
            id = id,
            caption = input.caption,
            isPrimary = input.isPrimary,
            displayOrder = input.displayOrder,
        )
    }

    @MutationMapping
    suspend fun deleteStayPicture(
        @Argument stayId: Int,
        @Argument id: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        stayService.requireOwnedByHost(stayId, currentUser.id)
        stayPictureService.deletePicture(stayId, id)
        return true
    }
}

data class UpdateStayPictureInput(
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
)
