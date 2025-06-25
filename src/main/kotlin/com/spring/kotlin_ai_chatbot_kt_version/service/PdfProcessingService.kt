package com.spring.kotlin_ai_chatbot_kt_version.service

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PdfProcessingService(
    private val vectorStore: VectorStore,
    @Value("\${app.pdf.processing.chunk-size:800}")
    private val chunkSize: Int,
    @Value("\${app.pdf.processing.chunk-overlap:100}")
    private val chunkOverlap: Int,
    @Value("\${app.pdf.processing.batch-size:50}")
    private val batchSize: Int
) {

    companion object {
        private val logger = LoggerFactory.getLogger(PdfProcessingService::class.java)
    }


    private fun enhanceChunksWithMetadata(chunks: List<Document>, filename: String?) {
        chunks.forEachIndexed { index, chunk ->
            chunk.metadata.apply {
                put("source", filename ?: "unknown")
            }
        }
    }

    data class ProcessingResult(
        val successful: Boolean,
        val documentsProcessed: Int = 0,
        val chunksCreated: Int = 0,
        val processingTimeMs: Long = 0,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(
                documentsProcessed: Int,
                chunksCreated: Int,
                processingTimeMs: Long
            ): ProcessingResult = ProcessingResult(
                successful = true,
                documentsProcessed = documentsProcessed,
                chunksCreated = chunksCreated,
                processingTimeMs = processingTimeMs
            )

            fun error(processingTimeMs: Long, errorMessage: String): ProcessingResult = ProcessingResult(
                successful = false,
                processingTimeMs = processingTimeMs,
                errorMessage = errorMessage
            )
        }
    }
}