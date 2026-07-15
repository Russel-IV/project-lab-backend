package com.team1.project_lab_backend.util

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

data class AuthenticatedPrincipal(val id: Int)

fun requireAuthenticated(): AuthenticatedPrincipal =
    SecurityContextHolder.getContext().authentication?.principal as? AuthenticatedPrincipal
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication required")
