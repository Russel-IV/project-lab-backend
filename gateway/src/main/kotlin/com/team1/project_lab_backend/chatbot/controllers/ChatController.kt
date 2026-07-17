package com.team1.project_lab_backend.chatbot.controllers

import com.team1.project_lab_backend.chatbot.services.ChatResponse
import com.team1.project_lab_backend.chatbot.services.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    data class ChatRequestDto(
        val message: String,
        val sessionId: String
    )

    @PostMapping
    fun chat(@RequestBody request: ChatRequestDto): ResponseEntity<ChatResponse> {
        return chatService.chat(request.message, request.sessionId)
    }

    @PostMapping("/ingest")
    fun ingestData(): ResponseEntity<Map<String, String>> {
        return chatService.ingestData()
    }
}
