package com.project.lab.chatbotService.service

import org.slf4j.LoggerFactory
import org.springframework.ai.reader.markdown.MarkdownDocumentReader
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Service
import java.io.File

/**
 * Service responsible for ingesting static knowledge files (like FRUI-CONTEXT.md)
 * into the Oracle Vector Store for platform FAQ retrieval using MarkdownDocumentReader.
 */
@Service
class IngestionService(
    private val vectorStore: VectorStore
) {
    private val logger = LoggerFactory.getLogger(IngestionService::class.java)

    /**
     * Reads the FRUI-CONTEXT.md from the root directory, parses its contents,
     * and loads them into the Oracle Vector Store.
     */
    fun ingestStaticGuidelines() {
        logger.info("Starting static FAQ guidelines ingestion using MarkdownDocumentReader...")
        val contextFile = File("FRUI-CONTEXT.md")
        if (!contextFile.exists()) {
            logger.warn("FRUI-CONTEXT.md not found in the root directory.")
            return
        }

        val resource = FileSystemResource(contextFile)
        val config = MarkdownDocumentReaderConfig.builder()
            .withHorizontalRuleCreateDocument(true) // Create a new document for each '---' divider
            .build()

        val reader = MarkdownDocumentReader(resource, config)
        val documents = reader.get()

        // Inject metadata identifiers
        documents.forEachIndexed { index, doc ->
            doc.metadata["source"] = "FRUI-CONTEXT.md"
            doc.metadata["sectionIndex"] = index.toString()
        }

        vectorStore.add(documents)
        logger.info("Successfully ingested ${documents.size} parsed Markdown chunks into the Vector Store.")
    }
}
