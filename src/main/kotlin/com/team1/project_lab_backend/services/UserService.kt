package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.UserRequest
import com.team1.project_lab_backend.dto.UserResponse
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createUser(request: UserRequest): UserResponse {
        request.name.requireNotBlank("name")
        val user = User(name = request.name)
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateUser(id: Int, request: UserRequest): UserResponse {
        id.requirePositive()
        request.name.requireNotBlank("name")
        userRepository.requireExistsById(id, "user not found")
        val user = User(id = id, name = request.name)
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun deleteUser(id: Int) {
        id.requirePositive()
        userRepository.requireExistsById(id, "user not found")
        userRepository.deleteById(id)
    }
}

private fun User.toResponse(): UserResponse =
    UserResponse(id = id, name = name)
