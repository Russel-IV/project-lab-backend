package com.team1.project_lab_backend.chatbot.services

import org.springframework.stereotype.Service

@Service
class ChatService(private val chatFeignClient: ChatFeignClient) {
    suspend fun chat(
        message: String,
        sessionId: String,
    ): ChatResponse = chatFeignClient.chat(ChatRequest(message, sessionId))

    suspend fun clearSession(sessionId: String) {
        chatFeignClient.clearSession(sessionId)
    }

    suspend fun ingestData(): Map<String, String> = chatFeignClient.ingestData()
}
