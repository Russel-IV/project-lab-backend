package com.project.lab.chatbotService.controller

import com.project.lab.chatbotService.service.ChatService
import com.project.lab.chatbotService.service.IngestionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller exposing REST endpoints for chatbot operations.
 */
@RestController
@RequestMapping("/internal/chat")
class ChatController(
    private val chatService: ChatService,
    private val ingestionService: IngestionService
) {

    /**
     * Data class representing the incoming chat request.
     */
    data class ChatRequest(
        val message: String,
        val sessionId: String
    )

    /**
     * Compact stay summary DTO for chatbot mini cards.
     */
    data class StaySummary(
        val id: Int,
        val publicId: String,
        val name: String,
        val propertyType: String,
        val starRating: java.math.BigDecimal? = null,
        val startingFromPrice: java.math.BigDecimal? = null,
        val city: String? = null,
        val countryCode: String? = null,
        val imageUrl: String? = null
    )

    /**
     * Data class representing the outgoing chat response.
     */
    data class ChatResponse(
        val response: String,
        val stays: List<StaySummary>? = null
    )

    /**
     * Endpoint to chat with the RAG chatbot.
     *
     * @param request the chat message and session identifier
     * @return the generated chatbot response
     */
    @PostMapping
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        if (request.message.isBlank() || request.sessionId.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val result = chatService.chat(request.message, request.sessionId)
        return ResponseEntity.ok(ChatResponse(response = result.text, stays = result.stays.takeIf { it.isNotEmpty() }))
    }

    /**
     * Endpoint to explicitly clear conversation memory for a session (Strategy A).
     *
     * @param sessionId the session identifier to clear
     * @return 204 No Content response
     */
    @DeleteMapping("/{sessionId}")
    fun clearSession(@PathVariable sessionId: String): ResponseEntity<Void> {
        if (sessionId.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        chatService.clearSession(sessionId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Endpoint to manually trigger database-to-vector store ingestion.
     *
     * @return response indicating the outcome of the ingestion process
     */
    @PostMapping("/ingest")
    fun ingestData(): ResponseEntity<Map<String, String>> {
        ingestionService.ingestStaticGuidelines()
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "Static knowledge ingestion triggered successfully."))
    }
}
