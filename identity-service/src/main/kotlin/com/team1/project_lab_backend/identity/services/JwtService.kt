package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.config.JwtProperties
import com.team1.project_lab_backend.identity.models.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

/**
 * Issues tokens now (docs/adr/0009, Phase 4) — every other service, including the
 * Gateway, only ever validates them locally via its own NimbusJwtDecoder pointed at
 * this same app.jwt.secret. Keep the signing algorithm pinned to HS256 explicitly:
 * the Gateway's decoder is pinned to match it exactly (see SecurityConfig.jwtDecoder
 * kdoc there for why that's not left to auto-configuration).
 */
@Service
class JwtService(private val jwtProperties: JwtProperties) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(user: User): String =
        Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiryMs))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
}
