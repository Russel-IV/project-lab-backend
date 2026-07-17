package com.project.lab.chatbotService.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

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
    private val chatClient: ChatClient
) {

    /**
     * Sends a chat message to the LLM, retrieving context from the Oracle Vector Store
     * and maintaining conversation history using the provided sessionId.
     *
     * @param message the user prompt
     * @param sessionId the conversational session ID
     * @return the LLM generated response
     */
    fun chat(message: String, sessionId: String): String {
        return chatClient.prompt()
            .user(message)
            .advisors { advisorSpec ->
                advisorSpec.param("chat_memory_conversation_id", sessionId)
            }
            .call()
            .content() ?: "I'm sorry, I could not process that request."
    }
}

