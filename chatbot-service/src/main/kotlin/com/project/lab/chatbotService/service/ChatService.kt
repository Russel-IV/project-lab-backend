package com.project.lab.chatbotService.service

import com.project.lab.chatbotService.context.ChatContext
import com.project.lab.chatbotService.controller.ChatController.StaySummary
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service

/**
 * Result data holder representing response text and stay recommendations.
 */
data class ChatResult(
    val text: String,
    val stays: List<StaySummary>
)

/**
 * Service to orchestrate conversational interaction with the LLM.
 * Relies on the default advisors configured at the ChatClient bean level.
 */
@Service
class ChatService(
    /**
     * The ChatClient is the core Spring AI abstraction used to communicate with the LLM (OpenAI model).
     * It is pre-configured in ChatConfig.kt, with default system instructions, advisors (memory and RAG), and tool calling configurations.
     */
    private val chatClient: ChatClient,
    /**
     * The ChatMemory store used to manage and clear conversation sessions.
     */
    private val chatMemory: ChatMemory,
    /**
     * Request-scoped context holder for stay search results.
     */
    private val chatContext: ChatContext
) {

    /**
     * Sends a chat message to the LLM, retrieving context from the Oracle Vector Store
     * and maintaining conversation history using the provided sessionId.
     *
     * @param message the user prompt
     * @param sessionId the conversational session ID
     * @return ChatResult containing the LLM text response and stay cards
     */
    fun chat(message: String, sessionId: String): ChatResult {
        chatContext.clear()
        val content = chatClient.prompt()
            .user(message)
            .advisors { advisorSpec ->
                advisorSpec.param("chat_memory_conversation_id", sessionId)
            }
            .call()
            .content() ?: "I'm sorry, I could not process that request."

        return ChatResult(
            text = content,
            stays = chatContext.recommendedStays
        )
    }

    /**
     * Explicitly clears the conversation memory for the given session ID (Strategy A).
     *
     * @param sessionId the session identifier to purge
     */
    fun clearSession(sessionId: String) {
        chatMemory.clear(sessionId)
    }
}

