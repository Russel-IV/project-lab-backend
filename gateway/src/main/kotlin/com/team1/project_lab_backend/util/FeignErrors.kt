package com.team1.project_lab_backend.util

import feign.FeignException
import tools.jackson.databind.ObjectMapper

private val objectMapper = ObjectMapper()

/**
 * Extracts the `message` field a downstream service's RestExceptionHandler/plain
 * ResponseStatusException body carries (server.error.include-message=always), so
 * a Gateway shim can re-throw with the same reason text instead of a generic one.
 */
fun feignErrorMessage(e: FeignException): String? = try {
    objectMapper.readTree(e.contentUTF8()).get("message")?.stringValue()
} catch (parseError: Exception) {
    null
}

/**
 * Extracts the `errors` map a downstream service's 422 FieldValidationException body
 * carries (identity-service's RestExceptionHandler), so a Gateway shim can re-throw a
 * local FieldValidationException with the same field/message pairs — preserving the
 * exact 422 { errors: {...} } contract external clients already see, byte-for-byte.
 */
@Suppress("UNCHECKED_CAST")
fun feignFieldErrors(e: FeignException): Map<String, String>? = try {
    val errorsNode = objectMapper.readTree(e.contentUTF8()).get("errors")
    if (errorsNode == null) null else objectMapper.convertValue(errorsNode, Map::class.java) as Map<String, String>
} catch (parseError: Exception) {
    null
}
