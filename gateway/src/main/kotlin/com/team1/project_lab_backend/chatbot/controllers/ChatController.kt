package com.team1.project_lab_backend.chatbot.controllers

import com.team1.project_lab_backend.chatbot.services.ChatResponse
import com.team1.project_lab_backend.chatbot.services.ChatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    data class ChatRequestDto(
        val message: String,
        val sessionId: String
    )

    @PostMapping
    suspend fun chat(@RequestBody request: ChatRequestDto): ChatResponse {
        return chatService.chat(request.message, request.sessionId)
    }

    @DeleteMapping("/{sessionId}")
    suspend fun clearSession(@PathVariable sessionId: String): org.springframework.http.ResponseEntity<Void> {
        chatService.clearSession(sessionId)
        return org.springframework.http.ResponseEntity.noContent().build()
    }

    @PostMapping("/ingest")
    suspend fun ingestData(): Map<String, String> {
        return chatService.ingestData()
    }
}
