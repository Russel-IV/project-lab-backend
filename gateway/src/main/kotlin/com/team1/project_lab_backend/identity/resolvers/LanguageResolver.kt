package com.team1.project_lab_backend.identity.resolvers

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.services.LanguageService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class LanguageResolver(
    private val languageService: LanguageService,
) {

    @QueryMapping
    fun languages(): List<Language> = languageService.getAllLanguages()

    @MutationMapping
    fun createLanguage(@Argument input: CreateLanguageInput): Language {
        requireAuthenticated()
        return languageService.createLanguage(LanguageRequest(languageName = input.languageName))
    }

    @MutationMapping
    fun updateLanguage(@Argument id: Int, @Argument input: UpdateLanguageInput): Language {
        requireAuthenticated()
        return languageService.updateLanguage(id, LanguageRequest(languageName = input.languageName))
    }

    @MutationMapping
    fun deleteLanguage(@Argument id: Int): Boolean {
        requireAuthenticated()
        languageService.deleteLanguage(id)
        return true
    }
}

data class CreateLanguageInput(val languageName: String)
data class UpdateLanguageInput(val languageName: String)
