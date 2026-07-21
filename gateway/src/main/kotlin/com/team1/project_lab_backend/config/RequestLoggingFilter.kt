package com.team1.project_lab_backend.config

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * docs/adr/0025: WebFilter replaces OncePerRequestFilter (servlet-only). Same
 * per-request timing/status log line as before; still no hand-written correlation
 * ID (docs/adr/0013 — Micrometer Tracing populates MDC's `traceId`).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class RequestLoggingFilter : WebFilter {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val start = System.currentTimeMillis()
        return chain.filter(exchange)
            .doFinally {
                val ms = System.currentTimeMillis() - start
                log.info(
                    "{} {} -> {} ({}ms)",
                    exchange.request.method,
                    exchange.request.uri.path,
                    exchange.response.statusCode,
                    ms,
                )
            }
    }
}
