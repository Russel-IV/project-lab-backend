package com.project.lab.chatbotService.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore

class IngestionServiceTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test ingestStaticGuidelines creates document chunks`() {
        // Arrange
        val vectorStore = mock(VectorStore::class.java)
        val ingestionService = IngestionService(vectorStore)

        // Act
        ingestionService.ingestStaticGuidelines()

        // Assert
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Document>>
        verify(vectorStore, times(1)).add(captor.capture())

        val documents = captor.value
        assertTrue(documents.isNotEmpty())

        val firstDoc = documents[0]
        assertEquals("FRUI-CONTEXT.md", firstDoc.metadata["source"])
        assertEquals("0", firstDoc.metadata["sectionIndex"])
        assertTrue(firstDoc.text!!.isNotEmpty())
    }
}
