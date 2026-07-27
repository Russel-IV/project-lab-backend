package com.team1.project_lab_backend.booking.config

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

/**
 * Without this, ResponseStatusException falls through to Spring's default /error
 * body, which omits "message" here despite server.error.include-message=always —
 * so the Gateway's webClientErrorMessage() (util/WebClientErrors.kt) always fails
 * to parse one and every downstream validation error collapses into its generic
 * per-call fallback string (e.g. "invalid payment intent").
 */
@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(ex.statusCode).body(mapOf("message" to ex.reason))
}
