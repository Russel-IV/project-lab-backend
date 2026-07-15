package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.repositories.UserRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
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
    fun getUserById(id: Int): User {
        id.requirePositive()
        return userRepository.findById(id).orNotFound("user not found")
    }

    @Transactional
    fun createUser(request: UserRequest): User {
        request.name.requireNotBlank("name")
        return userRepository.save(User(name = request.name))
    }

    @Transactional
    fun updateUser(id: Int, request: UserRequest, requestingUserId: Int): User {
        id.requirePositive()
        if (id != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        request.name.requireNotBlank("name")
        userRepository.requireExistsById(id, "user not found")
        return userRepository.save(User(id = id, name = request.name))
    }

    @Transactional
    fun deleteUser(id: Int, requestingUserId: Int) {
        id.requirePositive()
        if (id != requestingUserId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        userRepository.requireExistsById(id, "user not found")
        userRepository.deleteById(id)
    }
}
