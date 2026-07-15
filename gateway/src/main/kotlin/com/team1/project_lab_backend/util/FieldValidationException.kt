package com.team1.project_lab_backend.util

/**
 * Field-level validation failure for REST endpoints (e.g. "email already in
 * use") that a client can't catch with format validation alone. Rendered as
 * a 422 { errors: { field: message } } body by RestExceptionHandler —
 * distinct from ResponseStatusException, which stays a plain reason string.
 */
class FieldValidationException(val errors: Map<String, String>) : RuntimeException()
