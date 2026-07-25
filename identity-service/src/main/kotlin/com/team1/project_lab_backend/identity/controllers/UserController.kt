package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/users")
class UserController(private val userService: UserService) {
    @GetMapping
    fun list(
        @RequestParam(required = false) ids: List<Int>?,
    ): List<User> = if (ids != null) userService.getUsersByIds(ids) else userService.getAllUsers()

    @GetMapping("/by-public-id/{publicId}")
    fun getByPublicId(
        @PathVariable publicId: UUID,
    ): User = userService.getUserByPublicId(publicId)

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Int,
    ): User = userService.getUserById(id)

    @PostMapping
    fun create(
        @RequestBody request: UserRequest,
    ): ResponseEntity<User> = ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request))

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
        @RequestBody request: UserRequest,
    ): User = userService.updateUser(id, request, requestingUserId)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
        @RequestParam requestingUserId: Int,
    ) {
        userService.deleteUser(id, requestingUserId)
    }
}
