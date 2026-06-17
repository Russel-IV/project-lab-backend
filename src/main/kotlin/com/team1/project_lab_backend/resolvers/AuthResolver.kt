package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.models.User
import com.team1.project_lab_backend.services.AuthService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class AuthResolver(private val authService: AuthService) {

    @MutationMapping
    fun login(
        @Argument email: String,
        @Argument password: String,
    ): AuthPayload = authService.login(email, password)

    @MutationMapping
    fun signup(
        @Argument name: String,
        @Argument email: String,
        @Argument password: String,
    ): AuthPayload = authService.signup(name, email, password)
}

data class AuthPayload(val token: String, val user: User)
