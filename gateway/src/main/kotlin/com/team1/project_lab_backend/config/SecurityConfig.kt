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
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

/**
 * WebFlux equivalent of the pre-docs/adr/0025 SecurityConfig — see that ADR for
 * why the gateway (and only the gateway) is reactive. API shapes below were
 * verified against the actual spring-security-{config,oauth2-resource-server}
 * 7.0.5 jars (ServerHttpSecurity.OAuth2ResourceServerSpec, JwtSpec,
 * ServerBearerTokenAuthenticationConverter) rather than assumed symmetric with
 * the servlet-stack API, per this project's established "don't trust it just
 * works" caution around Spring Boot's auto-config splits.
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtProperties: JwtProperties,
    @Value("\${app.cors.allowed-origins}") private val corsAllowedOrigins: String,
) {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        jwtDecoder: ReactiveJwtDecoder,
    ): SecurityWebFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .authorizeExchange { auth -> auth.anyExchange().permitAll() }
            .headers { headers -> headers.frameOptions { it.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN) } }
            .oauth2ResourceServer { oauth2 ->
                oauth2
                    .bearerTokenConverter(bearerTokenConverter(jwtDecoder))
                    .jwt { jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
        return http.build()
    }

    /**
     * NimbusReactiveJwtDecoder wired explicitly for the same reason the MVC
     * config wired NimbusJwtDecoder explicitly — pinned to HS256 to match
     * JwtService's explicit signWith(key, Jwts.SIG.HS256).
     */
    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder
            .withSecretKey(SecretKeySpec(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    /**
     * Reactive equivalent of the MVC config's forgiving BearerTokenResolver.
     * ServerHttpSecurity has no bearerTokenResolver hook — the reactive resource
     * server instead takes a ServerAuthenticationConverter that must produce a
     * full (still-unauthenticated) Authentication, not just a token string,
     * which JwtSpec's jwtDecoder/jwtAuthenticationConverter then authenticate.
     * This wraps the stock ServerBearerTokenAuthenticationConverter and
     * pre-validates the extracted token, resolving to Mono.empty() (not an
     * error) when it doesn't decode — so a permitAll() route with a bad/expired
     * token is treated as "no token supplied" rather than rejected outright,
     * matching the old hand-written JwtAuthFilter's forgiving behavior.
     */
    private fun bearerTokenConverter(jwtDecoder: ReactiveJwtDecoder): ServerAuthenticationConverter {
        val default = ServerBearerTokenAuthenticationConverter()
        return ServerAuthenticationConverter { exchange ->
            default.convert(exchange)
                .cast(BearerTokenAuthenticationToken::class.java)
                .flatMap { token ->
                    jwtDecoder.decode(token.token)
                        .thenReturn(token as Authentication)
                        .onErrorResume(JwtException::class.java) { Mono.empty() }
                }
        }
    }

    /**
     * `sub` carries the user's opaque publicId (per JwtService.generateToken) so a
     * decoded token doesn't disclose the raw internal id; `uid` carries that internal
     * id in a separate claim so per-request internal plumbing (ownership checks, Feign
     * calls) doesn't need a network round trip to resolve one from the other. `uid` is
     * only ever readable by the token's own holder, so this isn't a new leak to anyone
     * else. JwtSpec requires a Mono-returning Converter (unlike the servlet-stack
     * Converter<Jwt, AbstractAuthenticationToken>).
     */
    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> =
        Converter { jwt ->
            val publicId =
                runCatching { UUID.fromString(jwt.subject) }.getOrNull()
                    ?: throw BadCredentialsException("invalid subject claim")
            val id =
                (jwt.claims["uid"] as? Number)?.toInt()
                    ?: throw BadCredentialsException("invalid uid claim")
            Mono.just(
                UsernamePasswordAuthenticationToken(AuthenticatedPrincipal(id, publicId), jwt.tokenValue, emptyList()),
            )
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
