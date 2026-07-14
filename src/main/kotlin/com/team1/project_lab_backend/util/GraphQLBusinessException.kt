package com.team1.project_lab_backend.util

import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

/**
 * A business-rule failure a GraphQL client needs to distinguish from a plain
 * reason string (e.g. NOT_ELIGIBLE vs ALREADY_REVIEWED) to drive specific UI
 * states. GraphQLExceptionHandler copies `code` into the error's `extensions`.
 */
class GraphQLBusinessException(
    val code: String,
    status: HttpStatusCode,
    reason: String,
) : ResponseStatusException(status, reason)
