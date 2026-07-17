package com.team1.project_lab_backend.chatbot.services

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ChatService(private val chatFeignClient: ChatFeignClient) {
    fun chat(message: String, sessionId: String): ResponseEntity<ChatResponse> {
        return chatFeignClient.chat(ChatRequest(message, sessionId))
    }

    fun ingestData(): ResponseEntity<Map<String, String>> {
        return chatFeignClient.ingestData()
    }
}
