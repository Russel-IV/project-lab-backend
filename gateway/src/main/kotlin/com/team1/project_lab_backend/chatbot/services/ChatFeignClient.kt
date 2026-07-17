package com.team1.project_lab_backend.chatbot.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "chatbot-service", contextId = "chatFeignClient")
interface ChatFeignClient {
    @PostMapping("/internal/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse>

    @PostMapping("/internal/chat/ingest")
    fun ingestData(): ResponseEntity<Map<String, String>>
}

data class ChatRequest(
    val message: String,
    val sessionId: String
)

data class ChatResponse(
    val response: String
)
