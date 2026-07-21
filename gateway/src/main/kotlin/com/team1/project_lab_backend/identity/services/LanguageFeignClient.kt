package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.Language
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class LanguageFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(): List<Language> =
        webClient.get().uri("/internal/languages").retrieve().awaitBody()

    suspend fun create(request: LanguageUpsertRequest): Language =
        webClient.post().uri("/internal/languages").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: LanguageUpsertRequest,
    ): Language =
        webClient.patch().uri("/internal/languages/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/languages/{id}", id).retrieve().awaitBodilessEntity()
    }
}

data class LanguageUpsertRequest(val languageName: String)
