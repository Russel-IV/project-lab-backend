package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.TravelerExperienceRequest
import com.team1.project_lab_backend.inventory.models.TravelerExperience
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class TravelerExperienceFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<TravelerExperience> =
        webClient.get()
            .uri { b ->
                b.path("/internal/traveler-experiences").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): TravelerExperience =
        webClient.get().uri("/internal/traveler-experiences/{id}", id).retrieve().awaitBody()

    suspend fun create(request: TravelerExperienceRequest): TravelerExperience =
        webClient.post().uri("/internal/traveler-experiences").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: TravelerExperienceRequest,
    ): TravelerExperience =
        webClient.patch().uri("/internal/traveler-experiences/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/traveler-experiences/{id}", id).retrieve().awaitBodilessEntity()
    }
}
