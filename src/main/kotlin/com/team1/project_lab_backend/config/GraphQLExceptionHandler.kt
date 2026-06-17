package com.team1.project_lab_backend.config

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class GraphQLExceptionHandler : DataFetcherExceptionResolverAdapter() {

    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (ex) {
            is ResponseStatusException -> GraphqlErrorBuilder.newError(env)
                .message(ex.reason ?: ex.message ?: "An error occurred")
                .errorType(ex.statusCode.toErrorType())
                .build()
            else -> null
        }
    }

    private fun HttpStatusCode.toErrorType(): ErrorType = when {
        this == HttpStatus.NOT_FOUND -> ErrorType.NOT_FOUND
        this == HttpStatus.BAD_REQUEST -> ErrorType.BAD_REQUEST
        this == HttpStatus.CONFLICT -> ErrorType.BAD_REQUEST
        this == HttpStatus.FORBIDDEN -> ErrorType.FORBIDDEN
        this == HttpStatus.UNAUTHORIZED -> ErrorType.UNAUTHORIZED
        else -> ErrorType.INTERNAL_ERROR
    }
}
