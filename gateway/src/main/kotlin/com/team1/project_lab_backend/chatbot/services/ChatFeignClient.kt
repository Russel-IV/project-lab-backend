package com.team1.project_lab_backend.chatbot.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import java.math.BigDecimal

@Component
class ChatFeignClient(
    @Qualifier("chatbotServiceWebClient") private val webClient: WebClient,
) {

    suspend fun chat(request: ChatRequest): ChatResponse =
        webClient.post().uri("/internal/chat").bodyValue(request).retrieve().awaitBody()

    suspend fun clearSession(sessionId: String) {
        webClient.delete().uri("/internal/chat/{sessionId}", sessionId).retrieve().awaitBodilessEntity()
    }

    suspend fun ingestData(): Map<String, String> =
        webClient.post().uri("/internal/chat/ingest").retrieve().awaitBody()
}

data class StaySummary(
    val id: Int,
    val publicId: String,
    val name: String,
    val propertyType: String,
    val starRating: BigDecimal? = null,
    val startingFromPrice: BigDecimal? = null,
    val city: String? = null,
    val countryCode: String? = null,
    val imageUrl: String? = null,
)

data class ChatRequest(
    val message: String,
    val sessionId: String,
)

data class ChatResponse(
    val response: String,
    val stays: List<StaySummary>? = null,
)
