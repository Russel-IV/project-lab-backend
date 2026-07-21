package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.AmenityRequest
import com.team1.project_lab_backend.inventory.models.Amenity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AmenityFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<Amenity> =
        webClient.get()
            .uri { b -> b.path("/internal/amenities").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Amenity =
        webClient.get().uri("/internal/amenities/{id}", id).retrieve().awaitBody()

    suspend fun create(request: AmenityRequest): Amenity =
        webClient.post().uri("/internal/amenities").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: AmenityRequest,
    ): Amenity =
        webClient.patch().uri("/internal/amenities/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/amenities/{id}", id).retrieve().awaitBodilessEntity()
    }
}
