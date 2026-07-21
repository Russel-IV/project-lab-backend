package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class DestinationFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(
        search: String? = null,
        limit: Int,
    ): List<Destination> =
        webClient.get()
            .uri { b ->
                b.path("/internal/destinations").queryParam("limit", limit)
                if (search != null) b.queryParam("search", search)
                b.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun popular(limit: Int): List<Destination> =
        webClient.get()
            .uri { b -> b.path("/internal/destinations/popular").queryParam("limit", limit).build() }
            .retrieve()
            .awaitBody()
}
