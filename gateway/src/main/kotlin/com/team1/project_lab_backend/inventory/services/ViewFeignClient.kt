package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.ViewRequest
import com.team1.project_lab_backend.inventory.models.View
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class ViewFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<View> =
        webClient.get()
            .uri { b -> b.path("/internal/views").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): View =
        webClient.get().uri("/internal/views/{id}", id).retrieve().awaitBody()

    suspend fun create(request: ViewRequest): View =
        webClient.post().uri("/internal/views").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: ViewRequest,
    ): View =
        webClient.patch().uri("/internal/views/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/views/{id}", id).retrieve().awaitBodilessEntity()
    }
}
