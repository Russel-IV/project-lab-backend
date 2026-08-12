package com.project.lab.chatbotService.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Expiring in-memory repository for chat session history.
 * Employs TTL Eviction using a Caffeine Cache to automatically
 * evict inactive conversation memory after 30 minutes of inactivity.
 */
@Component
class ExpiringInMemoryChatMemoryRepository : ChatMemoryRepository {

    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<String, List<Message>>()

    /**
     * Finds the stored messages for a specific conversation ID.
     *
     * @param conversationId the session identifier
     * @return list of stored messages or an empty list if none exist
     */
    override fun findByConversationId(conversationId: String): List<Message> {
        return cache.getIfPresent(conversationId) ?: emptyList()
    }

    /**
     * Stores or updates the list of messages for a specific conversation ID.
     *
     * @param conversationId the session identifier
     * @param messages the updated list of messages
     */
    override fun saveAll(conversationId: String, messages: List<Message>) {
        cache.put(conversationId, messages)
    }

    /**
     * Explicitly purges all messages stored for a specific conversation ID (Strategy A).
     *
     * @param conversationId the session identifier
     */
    override fun deleteByConversationId(conversationId: String) {
        cache.invalidate(conversationId)
    }

    /**
     * Returns all active conversation IDs stored in memory.
     *
     * @return list of conversation IDs
     */
    override fun findConversationIds(): List<String> {
        return cache.asMap().keys.toList()
    }
}
