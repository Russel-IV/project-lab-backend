package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.models.Favorite
import com.team1.project_lab_backend.review.repositories.FavoriteRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class FavoriteServiceTest {
    private val favoriteRepository = Mockito.mock(FavoriteRepository::class.java)
    private val favoriteService = FavoriteService(favoriteRepository)

    // ---- addFavorite ----

    @Test
    fun addFavoriteSavesWhenNotAlreadyFavorited() {
        Mockito.`when`(favoriteRepository.existsByUserIdAndStayId(1, 2)).thenReturn(false)

        favoriteService.addFavorite(1, 2)

        Mockito.verify(favoriteRepository).save(Favorite(userId = 1, stayId = 2))
    }

    @Test
    fun addFavoriteIsIdempotentWhenAlreadyFavorited() {
        Mockito.`when`(favoriteRepository.existsByUserIdAndStayId(1, 2)).thenReturn(true)

        favoriteService.addFavorite(1, 2)

        Mockito.verify(favoriteRepository, Mockito.never()).save(Mockito.any(Favorite::class.java))
    }

    // ---- removeFavorite ----

    @Test
    fun removeFavoriteDelegatesToRepositoryRegardlessOfPriorState() {
        favoriteService.removeFavorite(1, 2)

        Mockito.verify(favoriteRepository).deleteByUserIdAndStayId(1, 2)
    }

    // ---- getFavoriteStayIds ----

    @Test
    fun getFavoriteStayIdsReturnsStayIdsOnly() {
        val favorites =
            listOf(
                Favorite(id = 1, userId = 1, stayId = 10),
                Favorite(id = 2, userId = 1, stayId = 20),
            )
        Mockito.`when`(favoriteRepository.findByUserId(1)).thenReturn(favorites)

        val result = favoriteService.getFavoriteStayIds(1)

        assertEquals(listOf(10, 20), result)
    }

    @Test
    fun getFavoriteStayIdsReturnsEmptyListWhenNoneExist() {
        Mockito.`when`(favoriteRepository.findByUserId(1)).thenReturn(emptyList())

        val result = favoriteService.getFavoriteStayIds(1)

        assertEquals(emptyList<Int>(), result)
    }
}
