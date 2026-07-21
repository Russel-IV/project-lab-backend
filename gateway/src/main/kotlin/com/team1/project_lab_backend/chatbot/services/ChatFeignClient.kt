package com.team1.project_lab_backend.chatbot.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class ChatFeignClient(
    @Qualifier("chatbotServiceWebClient") private val webClient: WebClient,
) {

    suspend fun chat(request: ChatRequest): ChatResponse =
        webClient.post().uri("/internal/chat").bodyValue(request).retrieve().awaitBody()

    suspend fun ingestData(): Map<String, String> =
        webClient.post().uri("/internal/chat/ingest").retrieve().awaitBody()
}

data class ChatRequest(
    val message: String,
    val sessionId: String,
)

data class ChatResponse(
    val response: String,
)
