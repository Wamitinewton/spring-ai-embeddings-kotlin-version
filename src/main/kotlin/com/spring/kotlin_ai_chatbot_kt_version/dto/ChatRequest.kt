package com.spring.kotlin_ai_chatbot_kt_version.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotBlank(message = "Question cannot be empty")
    @field:Size(max = 1000, message = "Question must be less than 1000 characters")
    val question: String
)
