package com.team1.project_lab_backend.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.team1.project_lab_backend.util.currentUserOrNull
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.time.Duration

private const val RATE_LIMIT_EXCEEDED_BODY = """{"error":"rate limit exceeded"}"""

class RateLimitFilter(
    private val properties: RateLimitProperties,
) : WebFilter {
    private val buckets: Cache<String, Bucket> =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(20_000)
            .build()

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        if (!properties.enabled || exchange.request.uri.path.startsWith("/actuator/")) {
            return chain.filter(exchange)
        }
        return mono { currentUserOrNull()?.id?.toString() }
            .switchIfEmpty(mono { exchange.request.remoteAddress?.address?.hostAddress ?: "unknown" })
            .flatMap { key -> consume(key, exchange, chain) }
    }

    private fun consume(
        key: String,
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val bucket = buckets.get(key) { newBucket() }
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            return chain.filter(exchange)
        }
        val response = exchange.response
        response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        response.headers.set(
            HttpHeaders.RETRY_AFTER,
            ((probe.nanosToWaitForRefill + 999_999_999L) / 1_000_000_000L).toString(),
        )
        response.headers.contentType = MediaType.APPLICATION_JSON
        val buffer = response.bufferFactory().wrap(RATE_LIMIT_EXCEEDED_BODY.toByteArray(StandardCharsets.UTF_8))
        return response.writeWith(Mono.just(buffer))
    }

    private fun newBucket(): Bucket =
        Bucket.builder()
            .addLimit(Bandwidth.classic(properties.burstCapacity, Refill.greedy(properties.requestsPerMinute, Duration.ofMinutes(1))))
            .build()
}
