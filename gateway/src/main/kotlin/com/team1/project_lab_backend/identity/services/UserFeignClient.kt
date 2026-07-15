package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.User
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "identity-service", contextId = "userFeignClient")
interface UserFeignClient {

    @GetMapping("/internal/users")
    fun list(@RequestParam(required = false) ids: List<Int>?): List<User>

    @GetMapping("/internal/users/{id}")
    fun get(@PathVariable id: Int): User

    @PostMapping("/internal/users")
    fun create(@RequestBody request: UserUpsertRequest): User

    @PatchMapping("/internal/users/{id}")
    fun update(@PathVariable id: Int, @RequestParam requestingUserId: Int, @RequestBody request: UserUpsertRequest): User

    @DeleteMapping("/internal/users/{id}")
    fun delete(@PathVariable id: Int, @RequestParam requestingUserId: Int)
}

data class UserUpsertRequest(val name: String)
