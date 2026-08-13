package com.project.lab.chatbotService.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.lab.chatbotService.service.ChatService
import com.project.lab.chatbotService.service.IngestionService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import com.project.lab.chatbotService.service.ChatResult

@WebMvcTest(ChatController::class)
class ChatControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockitoBean
    private lateinit var chatService: ChatService

    @MockitoBean
    private lateinit var ingestionService: IngestionService

    @Test
    fun `test chat endpoint returns response`() {
        val request = ChatController.ChatRequest("Hello", "session-123")
        `when`(chatService.chat("Hello", "session-123")).thenReturn(ChatResult("Hi traveler!", emptyList()))

        mockMvc.perform(
            post("/internal/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.response").value("Hi traveler!"))

        verify(chatService).chat("Hello", "session-123")
    }

    @Test
    fun `test ingest endpoint triggers ingestion`() {
        mockMvc.perform(
            post("/internal/chat/ingest")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("Static knowledge ingestion triggered successfully."))

        verify(ingestionService).ingestStaticGuidelines()
    }
}
