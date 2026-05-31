package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.UserRequest
import com.team1.project_lab_backend.dto.UserResponse
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createUser(request: UserRequest): UserResponse {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        val user = User(name = request.name)
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateUser(id: Int, request: UserRequest): UserResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (!userRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
        val user = User(id = id, name = request.name)
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun deleteUser(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!userRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
        userRepository.deleteById(id)
    }
}

private fun User.toResponse(): UserResponse =
    UserResponse(id = id, name = name)
