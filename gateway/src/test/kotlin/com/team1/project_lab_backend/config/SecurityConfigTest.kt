package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.util.AuthenticatedPrincipal
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.UUID

private const val SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256"

/**
 * Confirms a token shaped like the one identity-service's JwtService issues (docs/
 * adr/0009, Phase 4 — JwtService moved there entirely, Gateway only ever validates)
 * actually decodes under SecurityConfig's NimbusReactiveJwtDecoder. Builds the token
 * directly via jjwt rather than depending on identity-service's JwtService class,
 * which no longer exists in this module. `.block()` is safe here — decoding a locally
 * signed HMAC token has no real I/O, it's just wrapped in Mono by the reactive API
 * (docs/adr/0025).
 */
class SecurityConfigTest {
    private val jwtProperties = JwtProperties(secret = SECRET, expiryMs = 60_000)
    private val securityConfig = SecurityConfig(jwtProperties, corsAllowedOrigins = "http://localhost:3000")
    private val decoder = securityConfig.jwtDecoder()

    private fun tokenFor(
        userId: Int,
        publicId: UUID,
        email: String,
    ): String {
        val key = Keys.hmacShaKeyFor(SECRET.toByteArray())
        return Jwts.builder()
            .subject(publicId.toString())
            .claim("uid", userId)
            .claim("email", email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiryMs))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    @Test
    fun tokenShapedLikeIdentityServicesDecodesUnderResourceServerConfig() {
        val publicId = UUID.randomUUID()
        val token = tokenFor(42, publicId, "ada@example.com")

        val jwt = decoder.decode(token).block()!!

        assertEquals(publicId.toString(), jwt.subject)
        assertEquals("ada@example.com", jwt.getClaimAsString("email"))
    }

    @Test
    fun decodedTokenConvertsToAuthenticatedPrincipal() {
        val publicId = UUID.randomUUID()
        val jwt = decoder.decode(tokenFor(7, publicId, "grace@example.com")).block()!!

        val authentication = securityConfig.jwtAuthenticationConverter().convert(jwt)?.block()

        assertEquals(AuthenticatedPrincipal(7, publicId), authentication?.principal)
    }
}
