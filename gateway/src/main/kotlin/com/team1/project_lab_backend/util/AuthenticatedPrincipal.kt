package com.team1.project_lab_backend.util

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class AuthenticatedPrincipal(val id: Int, val publicId: UUID)

/**
 * docs/adr/0025: ReactiveSecurityContextHolder threads the SecurityContext through
 * Reactor Context, not a ThreadLocal, so this must be suspend and every caller
 * (17 resolver files) must itself be a suspend fun for the context to propagate.
 */
suspend fun requireAuthenticated(): AuthenticatedPrincipal =
    currentUserOrNull() ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication required")

/**
 * Non-throwing counterpart to [requireAuthenticated], for the handful of call sites
 * (e.g. StayResolver's favoritesOnly filter) that need to behave differently for an
 * anonymous caller rather than reject the whole request outright.
 */
suspend fun currentUserOrNull(): AuthenticatedPrincipal? =
    ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()?.authentication?.principal as? AuthenticatedPrincipal
