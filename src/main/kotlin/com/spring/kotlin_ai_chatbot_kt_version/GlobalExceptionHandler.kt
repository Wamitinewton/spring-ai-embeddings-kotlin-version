package com.spring.kotlin_ai_chatbot_kt_version

import com.spring.kotlin_ai_chatbot_kt_version.dto.ChatResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ChatResponse> {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            fieldName to errorMessage
        }

        val errorMessage = "Validation failed: $errors"
        logger.warn("Validation error: {}", errorMessage)

        return ResponseEntity.badRequest().body(ChatResponse.error(errorMessage))
    }

    @ExceptionHandler(VectorStoreException::class)
    fun handleVectorStoreException(ex: VectorStoreException): ResponseEntity<ChatResponse> {
        logger.error("Vector store error: {}", ex.message, ex)

        val response = ChatResponse.error("There was an issue with the knowledge base. Please try again later.")
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
    }

    @ExceptionHandler(PdfProcessingException::class)
    fun handlePdfProcessingException(ex: PdfProcessingException): ResponseEntity<ChatResponse> {
        logger.error("PDF processing error: {}", ex.message, ex)

        val response = ChatResponse.error("Error processing PDF file: ${ex.message}")
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ChatResponse> {
        logger.error("Unexpected error occurred", ex)

        val response = ChatResponse.error("An unexpected error occurred. Please try again later.")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}

class VectorStoreException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class PdfProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)