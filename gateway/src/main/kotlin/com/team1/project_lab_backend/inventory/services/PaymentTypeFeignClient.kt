package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PaymentTypeFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<PaymentType> =
        webClient.get()
            .uri { b -> b.path("/internal/payment-types").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): PaymentType =
        webClient.get().uri("/internal/payment-types/{id}", id).retrieve().awaitBody()

    suspend fun create(request: PaymentTypeRequest): PaymentType =
        webClient.post().uri("/internal/payment-types").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: PaymentTypeRequest,
    ): PaymentType =
        webClient.patch().uri("/internal/payment-types/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/payment-types/{id}", id).retrieve().awaitBodilessEntity()
    }
}
