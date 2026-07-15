package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> = userRepository.findAll()

    @Transactional(readOnly = true)
    fun getUserById(id: Int): User =
        userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "user not found") }

    @Transactional(readOnly = true)
    fun getUsersByIds(ids: List<Int>): List<User> = userRepository.findAllById(ids)

    @Transactional
    fun createUser(request: UserRequest): User {
        if (request.name.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        return userRepository.save(User(name = request.name))
    }

    @Transactional
    fun updateUser(id: Int, request: UserRequest, requestingUserId: Int): User {
        if (id != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        if (request.name.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        if (!userRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        return userRepository.save(User(id = id, name = request.name))
    }

    @Transactional
    fun deleteUser(id: Int, requestingUserId: Int) {
        if (id != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        if (!userRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        userRepository.deleteById(id)
    }
}
