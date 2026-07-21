package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PropertyBrandRequest
import com.team1.project_lab_backend.inventory.models.PropertyBrand
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PropertyBrandFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<PropertyBrand> =
        webClient.get()
            .uri { b -> b.path("/internal/property-brands").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): PropertyBrand =
        webClient.get().uri("/internal/property-brands/{id}", id).retrieve().awaitBody()

    suspend fun create(request: PropertyBrandRequest): PropertyBrand =
        webClient.post().uri("/internal/property-brands").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: PropertyBrandRequest,
    ): PropertyBrand =
        webClient.patch().uri("/internal/property-brands/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/property-brands/{id}", id).retrieve().awaitBodilessEntity()
    }
}
