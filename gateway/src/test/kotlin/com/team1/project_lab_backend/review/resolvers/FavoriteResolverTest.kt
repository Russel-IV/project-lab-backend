package com.team1.project_lab_backend.review.resolvers

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.review.services.FavoriteService
import com.team1.project_lab_backend.util.AuthenticatedPrincipal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class FavoriteResolverTest {
    private val favoriteService = Mockito.mock(FavoriteService::class.java)
    private val resolver = FavoriteResolver(favoriteService)

    private val authenticatedUser = User(id = 1, name = "Alice", email = null)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun authenticateAs(user: User) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(AuthenticatedPrincipal(user.id), null, emptyList())
    }

    // ---- myFavoriteStayIds ----

    @Test
    fun myFavoriteStayIdsDelegatesToService() {
        authenticateAs(authenticatedUser)
        Mockito.`when`(favoriteService.getMyFavoriteStayIds(1)).thenReturn(listOf(10, 20))

        val result = resolver.myFavoriteStayIds()

        assertEquals(listOf(10, 20), result)
    }

    @Test
    fun myFavoriteStayIdsRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.myFavoriteStayIds() }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    // ---- addFavorite ----

    @Test
    fun addFavoriteReturnsTrueOnSuccess() {
        authenticateAs(authenticatedUser)
        Mockito.doNothing().`when`(favoriteService).addFavorite(eqArg(1), eqArg(2))

        val result = resolver.addFavorite(2)

        assertEquals(true, result)
        Mockito.verify(favoriteService).addFavorite(eqArg(1), eqArg(2))
    }

    @Test
    fun addFavoriteRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.addFavorite(2) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    // ---- removeFavorite ----

    @Test
    fun removeFavoriteReturnsTrueOnSuccess() {
        authenticateAs(authenticatedUser)
        Mockito.doNothing().`when`(favoriteService).removeFavorite(eqArg(1), eqArg(2))

        val result = resolver.removeFavorite(2)

        assertEquals(true, result)
        Mockito.verify(favoriteService).removeFavorite(eqArg(1), eqArg(2))
    }

    @Test
    fun removeFavoriteRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.removeFavorite(2) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }
}
