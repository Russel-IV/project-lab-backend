package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): User CRUD and its validation now live in
 * identity-service, reached via userFeignClient.
 */
@Service
class UserService(private val userFeignClient: UserFeignClient) {

    fun getAllUsers(): List<User> = userFeignClient.list(ids = null)

    fun getUserById(id: Int): User =
        try {
            userFeignClient.get(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

    fun createUser(request: UserRequest): User =
        try {
            userFeignClient.create(UserUpsertRequest(name = request.name))
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }

    fun updateUser(id: Int, request: UserRequest, requestingUserId: Int): User =
        try {
            userFeignClient.update(id, requestingUserId, UserUpsertRequest(name = request.name))
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

    fun deleteUser(id: Int, requestingUserId: Int) {
        try {
            userFeignClient.delete(id, requestingUserId)
        } catch (e: FeignException.Forbidden) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden")
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
    }
}
