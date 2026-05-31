package com.team1.project_lab_backend.dto

data class UserRequest(
    val name: String
)

data class UserResponse(
    val id: Int,
    val name: String
)
