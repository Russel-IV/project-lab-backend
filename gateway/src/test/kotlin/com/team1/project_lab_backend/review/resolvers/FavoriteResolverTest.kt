package com.team1.project_lab_backend.review.resolvers

import com.team1.project_lab_backend.review.services.FavoriteService
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.withAuthenticatedUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class FavoriteResolverTest {
    private val favoriteService = Mockito.mock(FavoriteService::class.java)
    private val resolver = FavoriteResolver(favoriteService)

    private val authenticatedUserId = 1

    // ---- myFavoriteStayIds ----

    @Test
    fun myFavoriteStayIdsDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(favoriteService.getMyFavoriteStayIds(1)).thenReturn(listOf(10, 20))

                val result = resolver.myFavoriteStayIds()

                assertEquals(listOf(10, 20), result)
            }
        }

    @Test
    fun myFavoriteStayIdsRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.myFavoriteStayIds() }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    // ---- addFavorite ----

    @Test
    fun addFavoriteReturnsTrueOnSuccess() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(favoriteService.addFavorite(eqArg(1), eqArg(2))).thenReturn(Unit)

                val result = resolver.addFavorite(2)

                assertEquals(true, result)
                Mockito.verify(favoriteService).addFavorite(eqArg(1), eqArg(2))
            }
        }

    @Test
    fun addFavoriteRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.addFavorite(2) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    // ---- removeFavorite ----

    @Test
    fun removeFavoriteReturnsTrueOnSuccess() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(favoriteService.removeFavorite(eqArg(1), eqArg(2))).thenReturn(Unit)

                val result = resolver.removeFavorite(2)

                assertEquals(true, result)
                Mockito.verify(favoriteService).removeFavorite(eqArg(1), eqArg(2))
            }
        }

    @Test
    fun removeFavoriteRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.removeFavorite(2) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }
}
