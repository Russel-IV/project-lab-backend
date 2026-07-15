package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.util.AuthenticatedPrincipal
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

private const val SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256"

/**
 * Confirms a token shaped like the one identity-service's JwtService issues (docs/
 * adr/0009, Phase 4 — JwtService moved there entirely, Gateway only ever validates)
 * actually decodes under SecurityConfig's NimbusJwtDecoder. Builds the token directly
 * via jjwt rather than depending on identity-service's JwtService class, which no
 * longer exists in this module — this is the one thing no other test exercises, since
 * a mismatch here (wrong algorithm, wrong key bytes) would only ever surface at
 * runtime as every request silently failing to authenticate.
 */
class SecurityConfigTest {

    private val jwtProperties = JwtProperties(secret = SECRET, expiryMs = 60_000)
    private val securityConfig = SecurityConfig(jwtProperties, corsAllowedOrigins = "http://localhost:3000")
    private val decoder = securityConfig.jwtDecoder()

    private fun tokenFor(userId: Int, email: String): String {
        val key = Keys.hmacShaKeyFor(SECRET.toByteArray())
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiryMs))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    @Test
    fun tokenShapedLikeIdentityServicesDecodesUnderResourceServerConfig() {
        val token = tokenFor(42, "ada@example.com")

        val jwt = decoder.decode(token)

        assertEquals("42", jwt.subject)
        assertEquals("ada@example.com", jwt.getClaimAsString("email"))
    }

    @Test
    fun decodedTokenConvertsToAuthenticatedPrincipal() {
        val jwt = decoder.decode(tokenFor(7, "grace@example.com"))

        val authentication = securityConfig.jwtAuthenticationConverter().convert(jwt)

        assertEquals(AuthenticatedPrincipal(7), authentication?.principal)
    }
}
