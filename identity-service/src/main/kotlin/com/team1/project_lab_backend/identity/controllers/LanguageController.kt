package com.team1.project_lab_backend.identity.controllers

import com.team1.project_lab_backend.identity.dto.LanguageRequest
import com.team1.project_lab_backend.identity.models.Language
import com.team1.project_lab_backend.identity.services.LanguageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/languages")
class LanguageController(private val languageService: LanguageService) {
    @GetMapping
    fun list(): List<Language> = languageService.getAllLanguages()

    @PostMapping
    fun create(
        @RequestBody request: LanguageRequest,
    ): ResponseEntity<Language> = ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(request))

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: LanguageRequest,
    ): Language = languageService.updateLanguage(id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Int,
    ) {
        languageService.deleteLanguage(id)
    }
}
