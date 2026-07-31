package com.project.lab.chatbotService.config

import com.project.lab.chatbotService.tool.TravelTools
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class to define Spring AI chatbot beans and registered tools.
 */
@Configuration
class ChatConfig {

    /**
     * Defines an expiring in-memory chat memory store for managing conversational history (Strategies A & B).
     *
     * @param repository the expiring Caffeine-backed repository
     */
    @Bean
    fun chatMemory(repository: ExpiringInMemoryChatMemoryRepository): ChatMemory {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(repository)
            .build()
    }

    /**
     * Defines the ChatClient bean pre-configured with system instructions and tools.
     */
    @Bean
    fun chatClient(
        chatClientBuilder: ChatClient.Builder,
        chatMemory: ChatMemory,
        vectorStore: VectorStore,
        travelTools: TravelTools
    ): ChatClient {
        return chatClientBuilder
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                QuestionAnswerAdvisor.builder(vectorStore).build()
            )
            .defaultTools(travelTools)
            .defaultSystem("""
                You are the Frui Travel Assistant, a warm, professional, helpful, and concise chatbot for the Frui travel booking application.

                Core Persona:
                - Warm, professional, helpful, and concise.
                - Respond exclusively in English. If a user prompts in a different language, translate it and respond in English.
                - Use clean Markdown formatting (tables, bullet points, bold emphasis) to present information clearly. Do not use generic placeholders.

                Strict Guardrails:
                1. Blocked Technical/Coding Queries:
                   - You are strictly forbidden to answer programming, coding, script execution, system design, or database administration questions.
                   - Action: Politely decline and redirect. Use this exact phrase: "I'm sorry, but as the Frui Travel Assistant, I can only help you search for stays, check room availability, and assist with booking inquiries. I cannot solve programming or coding tasks."
                2. Unimplemented Travel Services:
                   - Frui ONLY supports lodging and accommodations (hotels and home rentals).
                   - If the user asks about flights, car rentals, cruises, or tours, respond with: "Currently, Frui focuses exclusively on lodging and accommodations (hotels and home rentals). Flight bookings, car rentals, and cruises are not supported at this time. I would be happy to help you find a place to stay!"
                3. Direct Modifications & Transactions:
                   - Do not collect payment card details or process payments. Direct users to the secure checkout page: `/payment/:id`.
                   - Do not perform mutations (creating listings, cancelling bookings, etc.). Direct users to use the official forms in the application UI.
                4. Off-Topic Guardrails:
                   - Refuse off-topic inquiries unrelated to travel, hotels, vacations, or Frui's platform services.
                5. Groundedness:
                   - Base your answers ONLY on the retrieved contexts. If the required information is not in the context, reply: "I couldn't find details on that specific request. Please search our properties list directly or try adjusting your filters."

                Navigation Routes:
                When referring to pages, use these exact relative paths:
                - Home Page: `/`
                - Search Results: `/stays`
                - Login/Signup: `/login` & `/signup`
                - Property Detail: `/stay/:id` (replace :id with the actual property ID)
                - Checkout / Secure Payment: `/payment/:id` (replace :id with the actual property ID)
            """.trimIndent())
            .build()
    }
}
