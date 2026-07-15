package com.team1.project_lab_backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(1)
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val correlationId = request.getHeader("X-Correlation-Id")
            ?: UUID.randomUUID().toString().take(8)

        MDC.put("correlationId", correlationId)
        response.setHeader("X-Correlation-Id", correlationId)

        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val ms = System.currentTimeMillis() - start
            log.info("{} {} -> {} ({}ms) [{}]", request.method, request.requestURI, response.status, ms, correlationId)
            MDC.remove("correlationId")
        }
    }
}
