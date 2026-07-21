package com.team1.project_lab_backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.nio.file.Path

/**
 * docs/adr/0025: WebFluxConfigurer replaces WebMvcConfigurer. No MultipartConfigElement
 * bean needed — WebFlux streams multipart natively (Part/FilePart), no servlet-style
 * multi-part config to wire.
 */
@Configuration
class WebConfig(
    @Value("\${app.upload.dir}") private val uploadDir: String,
) : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = Path.of(uploadDir).toAbsolutePath().toString()
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$absolutePath/")
    }
}

/**
 * WebFlux has no HandlerInterceptor equivalent for this kind of path-scoped response
 * header — a WebFilter is the standard replacement.
 */
@Component
class UploadsContentDispositionFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        if (exchange.request.uri.path.startsWith("/uploads/")) {
            exchange.response.headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment")
        }
        return chain.filter(exchange)
    }
}
