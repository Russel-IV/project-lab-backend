package com.team1.project_lab_backend.inventory.resolvers

import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.services.StayService
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

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class StayResolverTest {
    private val stayService = Mockito.mock(StayService::class.java)
    private val resolver = StayResolver(stayService)
    private val authenticatedUser = User(id = 1, name = "Alice")

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun authenticateAs(user: User) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(AuthenticatedPrincipal(user.id), null, emptyList())
    }

    private fun sampleStay(id: Int = 1) =
        Stay(
            id = id,
            name = "Cozy Cabin",
            propertyType = PropertyType.HOME,
            host = Host(id = 42),
            address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
        )

    // ---- queries ----

    @Test
    fun staysPassesPageAndSizeToService() {
        val stays = listOf(sampleStay())
        Mockito.`when`(stayService.searchStays(anyArg(), eqArg(2), eqArg(5))).thenReturn(stays)

        val result = resolver.stays(null, 2, 5)

        assertEquals(1, result.size)
        assertEquals("Cozy Cabin", result[0].name)
        Mockito.verify(stayService).searchStays(anyArg(), eqArg(2), eqArg(5))
    }

    @Test
    fun staysReturnsEmptyListWhenNoResults() {
        Mockito.`when`(stayService.searchStays(anyArg(), eqArg(0), eqArg(20))).thenReturn(emptyList())

        val result = resolver.stays(null, 0, 20)

        assertEquals(0, result.size)
    }

    @Test
    fun stayByIdDelegatestoService() {
        Mockito.`when`(stayService.getStayById(7)).thenReturn(sampleStay(7))

        val result = resolver.stay(7)

        assertEquals(7, result?.id)
        assertEquals("Cozy Cabin", result?.name)
    }

    @Test
    fun stayByIdPropagatesNotFoundException() {
        Mockito.`when`(stayService.getStayById(99)).thenThrow(
            org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "stay not found",
            ),
        )

        assertThrows(ResponseStatusException::class.java) {
            resolver.stay(99)
        }
    }

    // ---- mutations ----

    @Test
    fun createStayDelegatesToService() {
        authenticateAs(authenticatedUser)
        val stay = sampleStay(10)
        Mockito.`when`(stayService.createStay(anyArg(), eqArg(1))).thenReturn(stay)

        val input =
            CreateStayInput(
                name = "Cozy Cabin",
                propertyType = PropertyType.HOME,
                address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                hostId = 42,
            )
        val result = resolver.createStay(input)

        assertEquals(10, result.id)
        Mockito.verify(stayService).createStay(anyArg(), eqArg(1))
    }

    @Test
    fun createStayRequiresAuthentication() {
        val input =
            CreateStayInput(
                name = "Cozy Cabin",
                propertyType = PropertyType.HOME,
                address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                hostId = 42,
            )
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.createStay(input) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun updateStayDelegatesToService() {
        authenticateAs(authenticatedUser)
        val stay = sampleStay(5)
        Mockito.`when`(stayService.updateStay(eqArg(5), anyArg(), eqArg(1))).thenReturn(stay)

        val input =
            UpdateStayInput(
                name = "Cozy Cabin",
                propertyType = PropertyType.HOME,
                address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                hostId = 42,
            )
        val result = resolver.updateStay(5, input)

        assertEquals(5, result.id)
        Mockito.verify(stayService).updateStay(eqArg(5), anyArg(), eqArg(1))
    }

    @Test
    fun updateStayRequiresAuthentication() {
        val input =
            UpdateStayInput(
                name = "Cozy Cabin",
                propertyType = PropertyType.HOME,
                address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                hostId = 42,
            )
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.updateStay(5, input) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }

    @Test
    fun deleteStayReturnsTrueOnSuccess() {
        authenticateAs(authenticatedUser)
        Mockito.doNothing().`when`(stayService).deleteStay(eqArg(1), eqArg(1))

        val result = resolver.deleteStay(1)

        assertEquals(true, result)
        Mockito.verify(stayService).deleteStay(eqArg(1), eqArg(1))
    }

    @Test
    fun deleteStayRequiresAuthentication() {
        val ex = assertThrows(ResponseStatusException::class.java) { resolver.deleteStay(1) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
    }
}
