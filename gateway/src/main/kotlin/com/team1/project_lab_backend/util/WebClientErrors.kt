package com.team1.project_lab_backend.util

import org.springframework.web.reactive.function.client.WebClientResponseException
import tools.jackson.databind.ObjectMapper

private val objectMapper = ObjectMapper()

/**
 * docs/adr/0025: WebClient equivalent of FeignErrors.kt's feignErrorMessage — same
 * downstream response body shape, different exception type. WebClientResponseException
 * has the same per-status subclasses Feign does (NotFound, BadRequest, Forbidden, ...),
 * so call sites just swap the caught type.
 */
fun webClientErrorMessage(e: WebClientResponseException): String? =
    try {
        objectMapper.readTree(e.responseBodyAsString).get("message")?.stringValue()
    } catch (parseError: Exception) {
        null
    }

@Suppress("UNCHECKED_CAST")
fun webClientFieldErrors(e: WebClientResponseException): Map<String, String>? =
    try {
        val errorsNode = objectMapper.readTree(e.responseBodyAsString).get("errors")
        if (errorsNode == null) null else objectMapper.convertValue(errorsNode, Map::class.java) as Map<String, String>
    } catch (parseError: Exception) {
        null
    }
