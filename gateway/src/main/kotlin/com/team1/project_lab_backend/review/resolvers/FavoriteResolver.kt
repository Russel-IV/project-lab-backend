package com.team1.project_lab_backend.review.resolvers

import com.team1.project_lab_backend.review.services.FavoriteService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class FavoriteResolver(private val favoriteService: FavoriteService) {
    @QueryMapping
    suspend fun myFavoriteStayIds(): List<Int> {
        val currentUser = requireAuthenticated()
        return favoriteService.getMyFavoriteStayIds(currentUser.id)
    }

    @MutationMapping
    suspend fun addFavorite(
        @Argument stayId: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        favoriteService.addFavorite(currentUser.id, stayId)
        return true
    }

    @MutationMapping
    suspend fun removeFavorite(
        @Argument stayId: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        favoriteService.removeFavorite(currentUser.id, stayId)
        return true
    }
}
