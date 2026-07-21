package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.Host
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class HostFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<Host> =
        webClient.get()
            .uri { b -> b.path("/internal/hosts").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Host =
        webClient.get().uri("/internal/hosts/{id}", id).retrieve().awaitBody()

    suspend fun create(request: HostUpsertRequest): Host =
        webClient.post().uri("/internal/hosts").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: HostUpsertRequest,
    ): Host =
        webClient.patch().uri("/internal/hosts/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/hosts/{id}", id).retrieve().awaitBodilessEntity()
    }
}

data class HostUpsertRequest(
    val id: Int? = null,
    val communicationRating: java.math.BigDecimal? = null,
    val checkinProcessRating: java.math.BigDecimal? = null,
    val cancellationRate: java.math.BigDecimal? = null,
    val languageIds: Set<Int> = emptySet(),
)
