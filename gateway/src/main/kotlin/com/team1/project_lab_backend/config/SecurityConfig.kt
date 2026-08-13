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
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
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

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties::class, RateLimitProperties::class)
class SecurityConfig(
    private val jwtProperties: JwtProperties,
    private val rateLimitProperties: RateLimitProperties,
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
            .addFilterAfter(RateLimitFilter(rateLimitProperties), SecurityWebFiltersOrder.AUTHENTICATION)
        return http.build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder
            .withSecretKey(SecretKeySpec(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

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
