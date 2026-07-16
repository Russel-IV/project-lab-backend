package com.team1.project_lab_backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Per-request timing/status log line. Correlation across requests/services no longer
 * needs hand-written ID generation/propagation (docs/adr/0013) — Micrometer Tracing
 * populates MDC's `traceId` automatically and propagates it across Feign calls with no
 * per-call-site code, and the console log pattern (`logging.pattern.console`) already
 * appends `[%X{traceId}]` to every line, this one included.
 */
@Component
@Order(1)
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val ms = System.currentTimeMillis() - start
            log.info("{} {} -> {} ({}ms)", request.method, request.requestURI, response.status, ms)
        }
    }
}
