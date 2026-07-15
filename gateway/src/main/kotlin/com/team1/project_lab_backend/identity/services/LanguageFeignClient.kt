package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.Language
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "identity-service", contextId = "languageFeignClient")
interface LanguageFeignClient {

    @GetMapping("/internal/languages")
    fun list(): List<Language>

    @PostMapping("/internal/languages")
    fun create(@RequestBody request: LanguageUpsertRequest): Language

    @PatchMapping("/internal/languages/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: LanguageUpsertRequest): Language

    @DeleteMapping("/internal/languages/{id}")
    fun delete(@PathVariable id: Int)
}

data class LanguageUpsertRequest(val languageName: String)
