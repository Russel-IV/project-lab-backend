package com.team1.project_lab_backend.controllers

import com.team1.project_lab_backend.dto.LanguageRequest
import com.team1.project_lab_backend.dto.LanguageResponse
import com.team1.project_lab_backend.services.LanguageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/languages")
class LanguageController(
    private val languageService: LanguageService
) {

    @GetMapping
    fun getAllLanguages(): ResponseEntity<List<LanguageResponse>> =
        ResponseEntity.ok(languageService.getAllLanguages())

    @PostMapping
    fun createLanguage(@RequestBody language: LanguageRequest): ResponseEntity<LanguageResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(language))

    @PutMapping("/{id}")
    fun updateLanguage(@PathVariable id: Int, @RequestBody language: LanguageRequest): ResponseEntity<LanguageResponse> =
        ResponseEntity.ok(languageService.updateLanguage(id, language))

    @DeleteMapping("/{id}")
    fun deleteLanguage(@PathVariable id: Int): ResponseEntity<Unit> =
        languageService.deleteLanguage(id).let { ResponseEntity.noContent().build() }
}
