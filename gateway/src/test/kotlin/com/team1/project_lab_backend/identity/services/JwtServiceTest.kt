package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.config.JwtProperties
import com.team1.project_lab_backend.config.SecurityConfig
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.util.AuthenticatedPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256"

/**
 * Confirms a token issued by JwtService actually decodes under SecurityConfig's
 * NimbusJwtDecoder — the one thing no existing test exercises, since the old
 * JwtAuthFilter parsed its own tokens with the same JwtService, but the new setup
 * relies on a separately-configured decoder (HS256, secret-key) actually matching
 * JwtService's signWith(key, Jwts.SIG.HS256). A mismatch here (wrong algorithm, wrong
 * key bytes) would only ever surface at runtime as every request silently failing to
 * authenticate.
 */
class JwtServiceTest {

    private val jwtProperties = JwtProperties(secret = SECRET, expiryMs = 60_000)
    private val jwtService = JwtService(jwtProperties)
    private val securityConfig = SecurityConfig(jwtProperties, corsAllowedOrigins = "http://localhost:3000")
    private val decoder = securityConfig.jwtDecoder()

    @Test
    fun tokenIssuedByJwtServiceDecodesUnderResourceServerConfig() {
        val user = User(id = 42, name = "Ada", email = "ada@example.com")

        val token = jwtService.generateToken(user)
        val jwt = decoder.decode(token)

        assertEquals("42", jwt.subject)
        assertEquals("ada@example.com", jwt.getClaimAsString("email"))
    }

    @Test
    fun decodedTokenConvertsToAuthenticatedPrincipal() {
        val user = User(id = 7, name = "Grace", email = "grace@example.com")
        val jwt = decoder.decode(jwtService.generateToken(user))

        val authentication = securityConfig.jwtAuthenticationConverter().convert(jwt)

        assertEquals(AuthenticatedPrincipal(7), authentication?.principal)
    }
}
