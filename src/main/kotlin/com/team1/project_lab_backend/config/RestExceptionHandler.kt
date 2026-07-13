package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.util.FieldValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(FieldValidationException::class)
    fun handleFieldValidation(ex: FieldValidationException): ResponseEntity<Map<String, Map<String, String>>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mapOf("errors" to ex.errors))
}
