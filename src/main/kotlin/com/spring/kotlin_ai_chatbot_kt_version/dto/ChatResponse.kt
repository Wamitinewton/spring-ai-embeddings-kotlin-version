package com.spring.kotlin_ai_chatbot_kt_version.dto

data class ChatResponse(
    val answer: String? = null,
    val confidence: String? = null,
    val contextDocumentCount: Int = 0,
    val responseTimeMs: Long = 0,
    val successful: Boolean = false,
    val errorMessage: String? = null,
){
    companion object {
        fun success(
            answer: String,
            confidence: String,
            contextDocumentsCount: Int,
            responseTimeMs: Long
        ): ChatResponse = ChatResponse(
            answer = answer,
            confidence = confidence,
            contextDocumentCount = contextDocumentsCount,
            responseTimeMs = responseTimeMs,
            successful = true
        )

        fun error(errorMessage: String): ChatResponse = ChatResponse(
            errorMessage = errorMessage,
            successful = false
        )
    }
}
