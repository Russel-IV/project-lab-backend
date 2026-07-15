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
