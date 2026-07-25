package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.config.JwtProperties
import com.team1.project_lab_backend.identity.models.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256"

class JwtServiceTest {
    private val jwtService = JwtService(JwtProperties(secret = SECRET, expiryMs = 60_000))

    @Test
    fun generateTokenUsesPublicIdAsSubjectAndInternalIdAsClaim() {
        val user = User(id = 42, name = "Ada", email = "ada@example.com")

        val token = jwtService.generateToken(user)
        val claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.toByteArray())).build().parseSignedClaims(token).payload

        assertEquals(user.publicId.toString(), claims.subject)
        assertEquals(42, (claims["uid"] as Number).toInt())
        assertEquals("ada@example.com", claims["email"])
    }
}
