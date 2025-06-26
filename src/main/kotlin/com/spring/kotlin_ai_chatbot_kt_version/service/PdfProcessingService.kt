package com.spring.kotlin_ai_chatbot_kt_version.service

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
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

    fun processPdfResource(pdfResource: Resource): ProcessingResult {
        val startTime = System.currentTimeMillis()

        return try {
            logger.info("Starting PDF processing for resource: {}", pdfResource.filename)

            val pdfReader = PagePdfDocumentReader(pdfResource)
            val documents = pdfReader.get()

            logger.info("Read {} pages from PDF", documents.size)

            val textSplitter = TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true)
            val chunks = textSplitter.apply(documents)

            logger.info("Created {} chunks from PDF content", chunks.size)

            enhanceChunksWithMetadata(chunks, pdfResource.filename)

            val totalChunks = chunks.size
            var processedChunks = 0

            chunks.chunked(batchSize).forEachIndexed { batchIndex, batch ->
                val startIndex = batchIndex * batchSize + 1
                val endIndex = startIndex + batch.size - 1

                logger.info("Processing batch {}-{} of {} chunks", startIndex, endIndex, totalChunks)
                vectorStore.add(batch)

                processedChunks += batch.size
                logger.debug("Processed {} chunks so far", processedChunks)
            }

            val processingTime = System.currentTimeMillis() - startTime
            logger.info("Successfully processed PDF. Total chunks: {}, Processing time: {}ms",
                processedChunks, processingTime)

            ProcessingResult.success(documents.size, processedChunks, processingTime)

        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            logger.error("Error processing PDF: {}", e.message, e)
            ProcessingResult.error(processingTime, e.message ?: "Unknown error")
        }
    }

    private fun enhanceChunksWithMetadata(chunks: List<Document>, filename: String?) {
        chunks.forEachIndexed { index, chunk ->
            chunk.metadata.apply {
                put("source", filename ?: "unknown")
                put("chunk_index", index)
                put("content_type", "kotlin_documentation")
                put("language", "kotlin")

                val content = chunk.text
                if (content != null) {
                    put("content_preview", if (content.length > 100) {
                        "${content.substring(0, 100)}..."
                    } else {
                        content
                    })
                }
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