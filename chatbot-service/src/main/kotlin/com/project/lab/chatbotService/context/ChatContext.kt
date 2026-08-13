package com.project.lab.chatbotService.context

import com.project.lab.chatbotService.controller.ChatController.StaySummary
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

/**
 * Request-scoped context holder to capture stay search results during a single LLM chat turn.
 */
@Component
@RequestScope
class ChatContext {
    private val _recommendedStays = mutableListOf<StaySummary>()

    val recommendedStays: List<StaySummary>
        get() = _recommendedStays.toList()

    fun setStays(stays: List<StaySummary>) {
        _recommendedStays.clear()
        _recommendedStays.addAll(stays.take(5))
    }

    fun clear() {
        _recommendedStays.clear()
    }
}
