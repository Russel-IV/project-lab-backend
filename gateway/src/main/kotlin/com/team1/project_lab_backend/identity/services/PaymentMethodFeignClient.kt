package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PaymentMethodFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(userId: Int): List<PaymentMethodResponse> =
        webClient.get()
            .uri { b -> b.path("/internal/payment-methods").queryParam("userId", userId).build() }
            .retrieve()
            .awaitBody()

    suspend fun create(
        userId: Int,
        request: PaymentMethodCreateRequest,
    ): PaymentMethodResponse =
        webClient.post()
            .uri { b -> b.path("/internal/payment-methods").queryParam("userId", userId).build() }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun setDefault(
        id: Int,
        userId: Int,
    ) {
        webClient.patch()
            .uri { b -> b.path("/internal/payment-methods/{id}/default").queryParam("userId", userId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }

    suspend fun delete(
        id: Int,
        userId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/payment-methods/{id}").queryParam("userId", userId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}

data class PaymentMethodCreateRequest(
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
)
