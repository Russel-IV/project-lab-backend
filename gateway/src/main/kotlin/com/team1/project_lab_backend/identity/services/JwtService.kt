package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.config.JwtProperties
import com.team1.project_lab_backend.identity.models.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(user: User): String = Jwts.builder()
        .subject(user.id.toString())
        .claim("email", user.email)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + jwtProperties.expiryMs))
        // Pinned to HS256 explicitly (rather than jjwt's length-based auto-selection)
        // so it matches the fixed algorithm the OAuth2 Resource Server's JwtDecoder is
        // configured for (spring.security.oauth2.resourceserver.jwt.jws-algorithms).
        .signWith(key, Jwts.SIG.HS256)
        .compact()

    fun extractUserId(token: String): Int? = try {
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toIntOrNull()
    } catch (e: JwtException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
