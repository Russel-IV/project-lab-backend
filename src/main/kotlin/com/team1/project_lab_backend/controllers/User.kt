package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.UserRequest
import com.team1.project_lab_backend.dto.UserResponse
import com.team1.project_lab_backend.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(userService.getAllUsers())

    @PostMapping
    fun createUser(@RequestBody user: UserRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user))

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Int, @RequestBody user: UserRequest): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.updateUser(id, user))

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Unit> =
        userService.deleteUser(id).let { ResponseEntity.noContent().build() }
}
