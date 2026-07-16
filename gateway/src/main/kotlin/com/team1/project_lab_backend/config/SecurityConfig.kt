package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.util.AuthenticatedPrincipal
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtProperties: JwtProperties,
    @Value("\${app.cors.allowed-origins}") private val corsAllowedOrigins: String,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
    ): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2
                    .bearerTokenResolver(bearerTokenResolver(jwtDecoder))
                    .jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
        return http.build()
    }

    /**
     * NimbusJwtDecoder wired explicitly (rather than relying on Boot's
     * spring.security.oauth2.resourceserver.jwt.secret-key auto-configuration, which
     * did not reliably produce a JwtDecoder bean under this Spring Boot 4.0.6 split of
     * spring-boot-security-oauth2-resource-server) — pinned to HS256 to match
     * JwtService's explicit signWith(key, Jwts.SIG.HS256).
     */
    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder
            .withSecretKey(SecretKeySpec(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    /**
     * All routes are permitAll() — auth is enforced downstream by requireAuthenticated()
     * (util/AuthenticatedPrincipal.kt), not by this filter chain. Spring Security's resource server
     * otherwise rejects a request with a 401 the moment it sees an invalid/expired Bearer
     * token, even on a permitAll route, which is a real behavior change from the previous
     * hand-written JwtAuthFilter (which just left such requests unauthenticated). This
     * resolver pre-validates the token and returns null instead of the raw value if it
     * doesn't decode, so the resource server filter treats it as "no token supplied"
     * rather than "bad credentials" — matching the old filter's forgiving behavior.
     */
    private fun bearerTokenResolver(jwtDecoder: JwtDecoder): BearerTokenResolver {
        val default = DefaultBearerTokenResolver()
        return BearerTokenResolver { request ->
            val token = default.resolve(request) ?: return@BearerTokenResolver null
            try {
                jwtDecoder.decode(token)
                token
            } catch (e: JwtException) {
                null
            }
        }
    }

    /**
     * Maps the decoded JWT's `sub` claim (the user id, per JwtService.generateToken) to
     * util.AuthenticatedPrincipal, so requireAuthenticated() is unchanged regardless of
     * which filter populates the SecurityContext.
     */
    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> =
        Converter { jwt ->
            val id =
                jwt.subject.toIntOrNull()
                    ?: throw BadCredentialsException("invalid subject claim")
            UsernamePasswordAuthenticationToken(AuthenticatedPrincipal(id), jwt.tokenValue, emptyList())
        }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = corsAllowedOrigins.split(",").map { it.trim() }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
