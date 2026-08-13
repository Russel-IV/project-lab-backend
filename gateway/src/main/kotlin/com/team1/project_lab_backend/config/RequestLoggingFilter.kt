package com.team1.project_lab_backend.config

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

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
