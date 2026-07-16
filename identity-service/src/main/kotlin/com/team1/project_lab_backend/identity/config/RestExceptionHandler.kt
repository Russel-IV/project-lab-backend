package com.team1.project_lab_backend.identity.config

import com.team1.project_lab_backend.util.FieldValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Renders the same 422 { errors: { field: message } } shape the Gateway's own
 * RestExceptionHandler used to produce directly — now identity-service produces it,
 * and the Gateway's ProfileService/PaymentMethodService shims re-throw a local
 * FieldValidationException from the parsed body (see util/FeignErrors.kt's
 * feignFieldErrors()) so external clients still see byte-for-byte the same response.
 */
@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(FieldValidationException::class)
    fun handleFieldValidation(ex: FieldValidationException): ResponseEntity<Map<String, Map<String, String>>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mapOf("errors" to ex.errors))
}
