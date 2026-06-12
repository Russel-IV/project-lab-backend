package com.team1.project_lab_backend.util

import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

fun Int.requirePositive(field: String = "id") {
    if (this <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be positive")
}

fun Int.requireNonNegative(field: String) {
    if (this < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be >= 0")
}

fun String.requireNotBlank(field: String) {
    if (this.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must not be blank")
}

fun Collection<Int>.requireAllPositive(field: String) {
    if (this.any { it <= 0 }) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must contain only positive ids")
}

fun BigDecimal.requireNonNegative(field: String) {
    if (this.compareTo(BigDecimal.ZERO) < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be >= 0")
}

fun BigDecimal.requireInRange(min: BigDecimal, max: BigDecimal, field: String) {
    if (this.compareTo(min) < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be >= $min")
    if (this.compareTo(max) > 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field must be <= $max")
}

fun <T> Optional<T>.orNotFound(message: String): T =
    orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, message) }

fun <T> Optional<T>.orBadRequest(message: String): T =
    orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, message) }

fun <T : Any, ID : Any> CrudRepository<T, ID>.requireExistsById(id: ID, message: String) {
    if (!existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, message)
}
