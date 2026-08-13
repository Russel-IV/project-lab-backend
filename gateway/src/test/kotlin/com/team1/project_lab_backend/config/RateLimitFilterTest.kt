package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.util.withAuthenticatedUser
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.InetSocketAddress

private val PASS_THROUGH = WebFilterChain { Mono.empty() }

private fun exchangeFrom(
    path: String = "/graphql",
    remoteAddress: String = "10.0.0.1",
): ServerWebExchange =
    MockServerWebExchange.from(
        MockServerHttpRequest.get(path).remoteAddress(InetSocketAddress(remoteAddress, 12345)),
    )

class RateLimitFilterTest {
    @Test
    fun requestsUnderTheLimitPassThrough() =
        runTest {
            val filter = RateLimitFilter(RateLimitProperties(requestsPerMinute = 5, burstCapacity = 5))
            val exchange = exchangeFrom()

            filter.filter(exchange, PASS_THROUGH).awaitSingleOrNull()

            assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.response.statusCode)
        }

    @Test
    fun rejectsOnceBurstIsExhausted() =
        runTest {
            val filter = RateLimitFilter(RateLimitProperties(requestsPerMinute = 1, burstCapacity = 1))

            filter.filter(exchangeFrom(), PASS_THROUGH).awaitSingleOrNull()
            val secondExchange = exchangeFrom()
            filter.filter(secondExchange, PASS_THROUGH).awaitSingleOrNull()

            assertEquals(HttpStatus.TOO_MANY_REQUESTS, secondExchange.response.statusCode)
            assertNotNull(secondExchange.response.headers.getFirst(HttpHeaders.RETRY_AFTER))
        }

    @Test
    fun keysByAuthenticatedUserIdRatherThanIp() =
        runTest {
            val filter = RateLimitFilter(RateLimitProperties(requestsPerMinute = 1, burstCapacity = 1))

            // Same IP for both, but different user ids — user 1 exhausting their bucket
            // must not affect user 2's.
            withAuthenticatedUser(userId = 1) {
                filter.filter(exchangeFrom(remoteAddress = "10.0.0.5"), PASS_THROUGH).awaitSingleOrNull()
            }
            val user2Exchange = exchangeFrom(remoteAddress = "10.0.0.5")
            withAuthenticatedUser(userId = 2) {
                filter.filter(user2Exchange, PASS_THROUGH).awaitSingleOrNull()
            }

            assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, user2Exchange.response.statusCode)
        }

    @Test
    fun actuatorHealthIsNeverLimited() =
        runTest {
            val filter = RateLimitFilter(RateLimitProperties(requestsPerMinute = 0, burstCapacity = 0))
            val exchange = exchangeFrom(path = "/actuator/health")

            filter.filter(exchange, PASS_THROUGH).awaitSingleOrNull()

            assertNull(exchange.response.statusCode)
        }

    @Test
    fun disabledBypassesLimitingEntirely() =
        runTest {
            val filter = RateLimitFilter(RateLimitProperties(enabled = false, requestsPerMinute = 0, burstCapacity = 0))
            val exchange = exchangeFrom()

            filter.filter(exchange, PASS_THROUGH).awaitSingleOrNull()

            assertNull(exchange.response.statusCode)
        }
}
