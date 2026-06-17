package com.team1.project_lab_backend.util

import com.team1.project_lab_backend.models.User
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

fun requireAuthenticated(): User =
    SecurityContextHolder.getContext().authentication?.principal as? User
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication required")
