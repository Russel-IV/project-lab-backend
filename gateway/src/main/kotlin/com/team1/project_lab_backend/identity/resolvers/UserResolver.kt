package com.team1.project_lab_backend.identity.resolvers

import com.team1.project_lab_backend.identity.dto.UserRequest
import com.team1.project_lab_backend.identity.models.User
import com.team1.project_lab_backend.identity.services.UserService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller

@Controller
class UserResolver(private val userService: UserService) {
    @QueryMapping
    fun users(): List<User> = userService.getAllUsers()

    @QueryMapping
    fun user(
        @Argument id: Int,
    ): User = userService.getUserById(id)

    @SchemaMapping(typeName = "User", field = "email")
    fun email(user: User): String? {
        val currentUser = SecurityContextHolder.getContext().authentication?.principal as? User
        return if (currentUser?.id == user.id) user.email else null
    }

    @MutationMapping
    fun createUser(
        @Argument input: CreateUserInput,
    ): User = userService.createUser(UserRequest(name = input.name))

    @MutationMapping
    fun updateUser(
        @Argument id: Int,
        @Argument input: UpdateUserInput,
    ): User {
        val currentUser = requireAuthenticated()
        return userService.updateUser(id, UserRequest(name = input.name), currentUser.id)
    }

    @MutationMapping
    fun deleteUser(
        @Argument id: Int,
    ): Boolean {
        val currentUser = requireAuthenticated()
        userService.deleteUser(id, currentUser.id)
        return true
    }
}

data class CreateUserInput(val name: String)

data class UpdateUserInput(val name: String)
