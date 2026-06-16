package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.UserRequest
import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.services.UserService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(private val userService: UserService) {

    @QueryMapping
    fun users(): List<User> = userService.getAllUsers()

    @QueryMapping
    fun user(@Argument id: Int): User? = userService.getUserById(id)

    @MutationMapping
    fun createUser(@Argument input: CreateUserInput): User =
        userService.createUser(UserRequest(name = input.name))

    @MutationMapping
    fun updateUser(@Argument id: Int, @Argument input: UpdateUserInput): User =
        userService.updateUser(id, UserRequest(name = input.name))

    @MutationMapping
    fun deleteUser(@Argument id: Int): Boolean {
        userService.deleteUser(id)
        return true
    }
}

data class CreateUserInput(val name: String)
data class UpdateUserInput(val name: String)
