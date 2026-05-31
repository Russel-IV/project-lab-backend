package com.team1.project_lab_backend.dto

data class LanguageRequest(
    val languageName: String
)

data class LanguageResponse(
    val id: Int,
    val languageName: String
)
