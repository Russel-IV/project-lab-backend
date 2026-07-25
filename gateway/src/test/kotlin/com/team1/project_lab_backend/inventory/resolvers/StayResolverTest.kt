package com.team1.project_lab_backend.inventory.resolvers

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.inventory.dto.StayConnection
import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayPriceStats
import com.team1.project_lab_backend.inventory.models.Address
import com.team1.project_lab_backend.inventory.models.PropertyType
import com.team1.project_lab_backend.inventory.models.Stay
import com.team1.project_lab_backend.inventory.services.StayService
import com.team1.project_lab_backend.review.services.FavoriteService
import com.team1.project_lab_backend.util.assertThrowsSuspend
import com.team1.project_lab_backend.util.withAuthenticatedUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

@Suppress("UNCHECKED_CAST")
private fun <T> anyArg(): T {
    Mockito.any<T>()
    return null as T
}

private fun <T> eqArg(value: T): T = Mockito.eq(value) ?: value

class StayResolverTest {
    private val stayService = Mockito.mock(StayService::class.java)
    private val favoriteService = Mockito.mock(FavoriteService::class.java)
    private val resolver = StayResolver(stayService, favoriteService)
    private val authenticatedUserId = 1

    private fun sampleStay(
        id: Int = 1,
        publicId: UUID = UUID.randomUUID(),
    ) = Stay(
        id = id,
        publicId = publicId,
        name = "Cozy Cabin",
        propertyType = PropertyType.HOME,
        hostId = 42,
        address = Address(id = 1, streetAddress = "1 Main St", city = "Springfield", countryCode = "US", regionId = 1),
    )

    // ---- queries ----

    @Test
    fun staysPassesPageAndSizeToService() =
        runTest {
            val connection = StayConnection(items = listOf(sampleStay()), totalCount = 1, hasNextPage = false)
            Mockito.`when`(stayService.searchStays(anyArg(), eqArg(2), eqArg(5))).thenReturn(connection)

            val result = resolver.stays(null, 2, 5)

        assertEquals(1, result.items.size)
        assertEquals("Cozy Cabin", result.items[0].name)
        Mockito.verify(stayService).searchStays(anyArg(), eqArg(2), eqArg(5))
    }

    @Test
    fun staysPassesRegionIdFilterToService() =
        runTest {
            val expectedFilter = StayFilter(regionId = 3)
            val connection = StayConnection(items = emptyList(), totalCount = 0, hasNextPage = false)
            Mockito.`when`(stayService.searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))).thenReturn(connection)

            resolver.stays(StayFilterInput(regionId = 3), 0, 20)

            Mockito.verify(stayService).searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))
        }

    @Test
    fun staysReturnsEmptyListWhenNoResults() =
        runTest {
            val connection = StayConnection(items = emptyList(), totalCount = 0, hasNextPage = false)
            Mockito.`when`(stayService.searchStays(anyArg(), eqArg(0), eqArg(20))).thenReturn(connection)

            val result = resolver.stays(null, 0, 20)

            assertEquals(0, result.items.size)
            assertEquals(0, result.totalCount)
        }

    @Test
    fun staysAppliesFavoritesOnlyForAuthenticatedUser() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                val expectedFilter = StayFilter(stayIds = listOf(5, 6))
                val connection = StayConnection(items = emptyList(), totalCount = 0, hasNextPage = false)
                Mockito.`when`(favoriteService.getMyFavoriteStayIds(authenticatedUserId)).thenReturn(listOf(5, 6))
                Mockito.`when`(stayService.searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))).thenReturn(connection)

                resolver.stays(StayFilterInput(favoritesOnly = true), 0, 20)

                Mockito.verify(stayService).searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))
            }
        }

    @Test
    fun staysFavoritesOnlyWithoutAuthResolvesToNoResultsWithoutCallingFavoriteService() =
        runTest {
            val expectedFilter = StayFilter(stayIds = emptyList())
            val connection = StayConnection(items = emptyList(), totalCount = 0, hasNextPage = false)
            Mockito.`when`(stayService.searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))).thenReturn(connection)

            resolver.stays(StayFilterInput(favoritesOnly = true), 0, 20)

            Mockito.verify(stayService).searchStays(eqArg(expectedFilter), eqArg(0), eqArg(20))
            Mockito.verify(favoriteService, Mockito.never()).getMyFavoriteStayIds(Mockito.anyInt())
        }

    @Test
    fun staysWithoutFavoritesOnlyNeverCallsFavoriteService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                val connection = StayConnection(items = emptyList(), totalCount = 0, hasNextPage = false)
                Mockito.`when`(stayService.searchStays(anyArg(), eqArg(0), eqArg(20))).thenReturn(connection)

                resolver.stays(StayFilterInput(regionId = 3), 0, 20)

                Mockito.verify(favoriteService, Mockito.never()).getMyFavoriteStayIds(Mockito.anyInt())
            }
        }

    @Test
    fun stayPriceStatsPassesFilterAndBinsToService() =
        runTest {
            val expectedFilter = StayFilter(regionId = 3)
            val stats = StayPriceStats(min = BigDecimal("10"), max = BigDecimal("90"), count = 2, histogram = listOf(1, 1))
            Mockito.`when`(stayService.getPriceStats(eqArg(expectedFilter), eqArg(10))).thenReturn(stats)

            val result = resolver.stayPriceStats(StayFilterInput(regionId = 3), 10)

            assertEquals(BigDecimal("10"), result.min)
            assertEquals(2, result.count)
            Mockito.verify(stayService).getPriceStats(eqArg(expectedFilter), eqArg(10))
        }

    @Test
    fun stayPriceStatsDefaultsBinsTo40WhenOmitted() =
        runTest {
            val stats = StayPriceStats(min = null, max = null, count = 0, histogram = List(40) { 0 })
            Mockito.`when`(stayService.getPriceStats(anyArg(), eqArg(40))).thenReturn(stats)

            resolver.stayPriceStats(null, null)

            Mockito.verify(stayService).getPriceStats(anyArg(), eqArg(40))
        }

    @Test
    fun stayByIdDelegatestoService() =
        runTest {
            Mockito.`when`(stayService.getStayById(7)).thenReturn(sampleStay(7))

            val result = resolver.stay(7)

            assertEquals(7, result?.id)
            assertEquals("Cozy Cabin", result?.name)
        }

    @Test
    fun stayByIdPropagatesNotFoundException() =
        runTest {
            Mockito.`when`(stayService.getStayById(99)).thenThrow(
                ResponseStatusException(HttpStatus.NOT_FOUND, "stay not found"),
            )

            assertThrowsSuspend<ResponseStatusException> { resolver.stay(99) }
        }

    @Test
    fun stayByPublicIdDelegatesToService() =
        runTest {
            val publicId = UUID.randomUUID()
            Mockito.`when`(stayService.getStayByPublicId(publicId)).thenReturn(sampleStay(publicId = publicId))

            val result = resolver.stayByPublicId(publicId)

            assertEquals(publicId, result?.publicId)
        }

    // ---- mutations ----

    @Test
    fun createStayDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
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
        }

    @Test
    fun createStayRequiresAuthentication() =
        runTest {
            val input =
                CreateStayInput(
                    name = "Cozy Cabin",
                    propertyType = PropertyType.HOME,
                    address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                    hostId = 42,
                )
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.createStay(input) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    @Test
    fun updateStayDelegatesToService() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
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
        }

    @Test
    fun updateStayRequiresAuthentication() =
        runTest {
            val input =
                UpdateStayInput(
                    name = "Cozy Cabin",
                    propertyType = PropertyType.HOME,
                    address = StayAddressInput(streetAddress = "1 Main St", city = "Springfield", countryCode = "US"),
                    hostId = 42,
                )
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.updateStay(5, input) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }

    @Test
    fun deleteStayReturnsTrueOnSuccess() =
        runTest {
            withAuthenticatedUser(authenticatedUserId) {
                Mockito.`when`(stayService.deleteStay(eqArg(1), eqArg(1))).thenReturn(Unit)

                val result = resolver.deleteStay(1)

                assertEquals(true, result)
                Mockito.verify(stayService).deleteStay(eqArg(1), eqArg(1))
            }
        }

    @Test
    fun deleteStayRequiresAuthentication() =
        runTest {
            val ex = assertThrowsSuspend<ResponseStatusException> { resolver.deleteStay(1) }
            assertEquals(HttpStatus.UNAUTHORIZED, ex.statusCode)
        }
}
