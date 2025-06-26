package com.spring.kotlin_ai_chatbot_kt_version.service

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KotlinChatbotService(
    private val chatModel: ChatModel,
    private val vectorStore: VectorStore,
    @Value("\${app.chatbot.max-context-documents:5}")
    private val maxContextDocuments: Int
) {

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinChatbotService::class.java)

        private const val KOTLIN_EXPERT_PROMPT = """
            You are a highly knowledgeable Kotlin expert and teacher. Your goal is to provide accurate,
            comprehensive, and educational answers about Kotlin programming.

            Based on the provided context from official Kotlin documentation, answer the user's question.

            Guidelines:
            1. Provide clear, accurate, and well-structured answers
            2. Include code examples when relevant
            3. Explain concepts in a teaching manner
            4. If the context doesn't contain enough information, say so clearly
            5. Focus specifically on Kotlin-related topics
            6. Structure your response in a clear, educational format

            Context from Kotlin Documentation:
            {context}

            User Question: {question}

            Answer:
        """
    }

    init {
        logger.info("KotlinChatbotService initialized with max context documents: {}", maxContextDocuments)
    }

    fun askQuestion(question: String): ChatbotResponse {
        val startTime = System.currentTimeMillis()

        return try {
            logger.info("Processing question: {}", question)

            if (question.isBlank()) {
                return ChatbotResponse.error("Question cannot be empty")
            }

            val relevantDocs = findRelevantDocuments(question)
            logger.info("Found {} relevant documents", relevantDocs.size)

            if (relevantDocs.isEmpty()) {
                return ChatbotResponse.error(
                    "I couldn't find relevant information in my Kotlin knowledge base. " +
                            "Please ask questions related to Kotlin programming, or ensure the knowledge base is properly loaded."
                )
            }

            val context = createContextFromDocuments(relevantDocs)
            logger.debug("Created context with {} characters", context.length)

            val answer = generateAnswer(question, context)
            val confidence = calculateConfidence(relevantDocs, answer)
            val responseTime = System.currentTimeMillis() - startTime

            logger.info("Generated response in {}ms with {} context documents, confidence: {}",
                responseTime, relevantDocs.size, confidence)

            ChatbotResponse.success(answer, confidence, relevantDocs.size, responseTime)

        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            logger.error("Error processing question after {}ms: {}", responseTime, e.message, e)
            ChatbotResponse.error("I encountered an error while processing your question. Please try again.")
        }
    }

    private fun findRelevantDocuments(question: String): List<Document> {
        return try {
            val searchRequest = SearchRequest.builder()
                .query(question)
                .topK(maxContextDocuments)
                .similarityThreshold(0.7)
                .build()

            val documents = vectorStore.similaritySearch(searchRequest) ?: emptyList()
            logger.debug("Vector search returned {} documents for query: {}", documents.size, question)

            documents
        } catch (e: Exception) {
            logger.error("Error searching vector store: {}", e.message, e)
            throw RuntimeException("Failed to search knowledge base", e)
        }
    }

    private fun createContextFromDocuments(documents: List<Document>): String {
        return documents.joinToString("\n${"=".repeat(50)}\n") { doc ->
            buildString {
                val metadata = doc.metadata

                metadata["source"]?.let { append("Source: $it\n") }
                metadata["chunk_index"]?.let { append("Section: $it\n") }
                append(doc.text)
                append("\n\n")
            }
        }
    }

    private fun generateAnswer(question: String, context: String): String {
        return try {
            val promptTemplate = PromptTemplate(KOTLIN_EXPERT_PROMPT)
            val promptVariables = mapOf(
                "context" to context,
                "question" to question
            )

            val prompt = promptTemplate.create(promptVariables)
            val response = chatModel.call(prompt)

            val answer = response.result.output.text
            logger.debug("Generated answer with {} characters", answer?.length)

            answer ?: ""
        } catch (e: Exception) {
            logger.error("Error generating answer with chat model: {}", e.message, e)
            throw RuntimeException("Failed to generate response", e)
        }
    }

    private fun calculateConfidence(documents: List<Document>, answer: String): String {
        if (documents.isEmpty()) return "LOW"

        val docCount = documents.size
        val answerLength = answer.length

        return when {
            docCount >= 4 && answerLength > 300 -> "HIGH"
            docCount >= 2 && answerLength > 150 -> "MEDIUM"
            docCount >= 1 && answerLength > 50 -> "LOW"
            else -> "VERY_LOW"
        }
    }

    data class ChatbotResponse(
        val successful: Boolean,
        val answer: String? = null,
        val confidence: String? = null,
        val contextDocumentsCount: Int = 0,
        val responseTimeMs: Long = 0,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(
                answer: String,
                confidence: String,
                contextDocumentsCount: Int,
                responseTimeMs: Long
            ): ChatbotResponse = ChatbotResponse(
                successful = true,
                answer = answer,
                confidence = confidence,
                contextDocumentsCount = contextDocumentsCount,
                responseTimeMs = responseTimeMs
            )

            fun error(errorMessage: String): ChatbotResponse = ChatbotResponse(
                successful = false,
                errorMessage = errorMessage
            )
        }
    }
}