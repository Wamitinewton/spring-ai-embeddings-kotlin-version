package com.spring.kotlin_ai_chatbot_kt_version.controller

import com.spring.kotlin_ai_chatbot_kt_version.dto.ChatRequest
import com.spring.kotlin_ai_chatbot_kt_version.dto.ChatResponse
import com.spring.kotlin_ai_chatbot_kt_version.dto.ChatbotInfo
import com.spring.kotlin_ai_chatbot_kt_version.service.KotlinChatbotService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kotlin-chatbot")
@CrossOrigin(origins = ["*"])
class KotlinChatbotController(
    private val chatbotService: KotlinChatbotService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinChatbotController::class.java)
    }

    @PostMapping("/ask")
    fun askQuestion(@Valid @RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        logger.info("Received question: {}", request.question)

        return try {
            val response = chatbotService.askQuestion(request.question)

            if (response.successful) {
                val chatResponse = ChatResponse.success(
                    response.answer!!,
                    response.confidence!!,
                    response.contextDocumentsCount,
                    response.responseTimeMs
                )
                ResponseEntity.ok(chatResponse)
            } else {
                val errorResponse = ChatResponse.error(response.errorMessage!!)
                ResponseEntity.badRequest().body(errorResponse)
            }

        } catch (e: Exception) {
            logger.error("Unexpected error processing question", e)
            val errorResponse = ChatResponse.error("An unexpected error occurred. Please try again.")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

    @GetMapping("/ask")
    fun askQuestionGet(@RequestParam question: String): ResponseEntity<ChatResponse> {
        logger.info("Received GET question: {}", question)

        return when {
            question.isBlank() -> {
                val errorResponse = ChatResponse.error("Question cannot be empty")
                ResponseEntity.badRequest().body(errorResponse)
            }
            question.length > 1000 -> {
                val errorResponse = ChatResponse.error("Question must be less than 1000 characters")
                ResponseEntity.badRequest().body(errorResponse)
            }
            else -> {
                val request = ChatRequest(question.trim())
                askQuestion(request)
            }
        }
    }

    @GetMapping("/info")
    fun getChatbotInfo(): ResponseEntity<ChatbotInfo> {
        val info = ChatbotInfo(
            name = "Kotlin Expert Chatbot",
            version = "1.0.0",
            description = "A specialized AI chatbot for answering Kotlin programming questions using RAG (Retrieval-Augmented Generation)",
            usage = "Ask me anything about Kotlin programming - syntax, concepts, best practices, and more!"
        )
        return ResponseEntity.ok(info)
    }
}