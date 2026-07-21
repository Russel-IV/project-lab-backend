package com.team1.project_lab_backend.review.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class FavoriteFeignClient(
    @Qualifier("reviewServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(userId: Int): List<Int> =
        webClient.get()
            .uri { b -> b.path("/internal/favorites").queryParam("userId", userId).build() }
            .retrieve()
            .awaitBody()

    suspend fun add(
        userId: Int,
        stayId: Int,
    ) {
        webClient.post()
            .uri { b -> b.path("/internal/favorites").queryParam("userId", userId).queryParam("stayId", stayId).build() }
            .retrieve()
            .awaitBodilessEntity()
    }

    suspend fun remove(
        userId: Int,
        stayId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/favorites").queryParam("userId", userId).queryParam("stayId", stayId).build() }
            .retrieve()
            .awaitBodilessEntity()
    }
}
