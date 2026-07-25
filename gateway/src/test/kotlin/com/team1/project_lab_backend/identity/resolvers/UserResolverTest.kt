package com.team1.project_lab_backend.identity.resolvers

import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class UserResolverTest {
    private val userService = Mockito.mock(UserService::class.java)
    private val resolver = UserResolver(userService)

    private fun sampleUser(
        id: Int = 1,
        publicId: UUID = UUID.randomUUID(),
    ) = User(id = id, publicId = publicId, name = "Ada", email = null)

    @Test
    fun userByIdDelegatesToService() =
        runTest {
            Mockito.`when`(userService.getUserById(7)).thenReturn(sampleUser(id = 7))

            val result = resolver.user(7)

            assertEquals(7, result.id)
        }

    @Test
    fun userByPublicIdDelegatesToService() =
        runTest {
            val publicId = UUID.randomUUID()
            Mockito.`when`(userService.getUserByPublicId(publicId)).thenReturn(sampleUser(publicId = publicId))

            val result = resolver.userByPublicId(publicId)

            assertEquals(publicId, result.publicId)
        }
}
