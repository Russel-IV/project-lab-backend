package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.StayFilter
import com.team1.project_lab_backend.inventory.dto.StayRequest
import com.team1.project_lab_backend.inventory.models.Stay
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class StayFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<Stay> =
        webClient.get()
            .uri { b -> b.path("/internal/stays").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Stay =
        webClient.get().uri("/internal/stays/{id}", id).retrieve().awaitBody()

    suspend fun search(
        filter: StayFilter,
        page: Int,
        size: Int,
    ): List<Stay> =
        webClient.post()
            .uri { b -> b.path("/internal/stays/search").queryParam("page", page).queryParam("size", size).build() }
            .bodyValue(filter)
            .retrieve()
            .awaitBody()

    suspend fun create(
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        webClient.post()
            .uri { b -> b.path("/internal/stays").queryParam("requestingUserId", requestingUserId).build() }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun update(
        id: Int,
        request: StayRequest,
        requestingUserId: Int,
    ): Stay =
        webClient.patch()
            .uri { b -> b.path("/internal/stays/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun delete(
        id: Int,
        requestingUserId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/stays/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}
