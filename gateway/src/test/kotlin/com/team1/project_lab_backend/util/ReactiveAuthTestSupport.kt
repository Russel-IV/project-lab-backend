package com.team1.project_lab_backend.util

import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import java.util.UUID

/**
 * docs/adr/0025: replaces the old `SecurityContextHolder.getContext().authentication =
 * ...` + `@AfterEach clearSecurityContext()` pattern (still documented in CLAUDE.md
 * pre-migration). ReactiveSecurityContextHolder threads the SecurityContext through
 * Reactor Context, not a ThreadLocal, so there's no global mutable state to set or
 * clear — auth is scoped to exactly the `block` passed here, per call.
 */
suspend fun <T> withAuthenticatedUser(
    userId: Int,
    block: suspend () -> T,
): T = withAuthenticatedUser(userId, UUID.randomUUID(), block)

suspend fun <T> withAuthenticatedUser(
    userId: Int,
    publicId: UUID,
    block: suspend () -> T,
): T =
    withContext(
        ReactorContext(
            ReactiveSecurityContextHolder.withAuthentication(
                UsernamePasswordAuthenticationToken(AuthenticatedPrincipal(userId, publicId), null, emptyList()),
            ),
        ),
    ) {
        block()
    }

/**
 * JUnit 5's Assertions.assertThrows takes a synchronous Executable, which a suspend
 * block under test can't satisfy directly. This is the coroutine-native equivalent
 * used across the suite's resolver/service/controller tests instead.
 */
suspend inline fun <reified T : Throwable> assertThrowsSuspend(crossinline block: suspend () -> Unit): T {
    try {
        block()
    } catch (e: Throwable) {
        if (e is T) return e
        throw AssertionError("Expected ${T::class.simpleName} but got ${e::class.simpleName}: ${e.message}", e)
    }
    throw AssertionError("Expected ${T::class.simpleName} to be thrown, but nothing was thrown")
}
