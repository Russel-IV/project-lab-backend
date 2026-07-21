package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AccessibilityRequest
import com.team1.project_lab_backend.inventory.models.Accessibility
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AccessibilityFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<Accessibility> =
        webClient.get()
            .uri { b -> b.path("/internal/accessibilities").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Accessibility =
        webClient.get().uri("/internal/accessibilities/{id}", id).retrieve().awaitBody()

    suspend fun create(request: AccessibilityRequest): Accessibility =
        webClient.post().uri("/internal/accessibilities").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: AccessibilityRequest,
    ): Accessibility =
        webClient.patch().uri("/internal/accessibilities/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/accessibilities/{id}", id).retrieve().awaitBodilessEntity()
    }
}
