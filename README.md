# Kotlin AI Chatbot with Spring AI

A modern, clean, and well-structured **Kotlin Spring Boot application** that implements a RAG (Retrieval-Augmented Generation) chatbot specialized in answering Kotlin programming questions.

## 🚀 Features

- **Kotlin-First Design**: Built entirely in Kotlin with idiomatic code patterns
- **RAG Implementation**: Uses vector embeddings and similarity search for context-aware responses
- **Qdrant Vector Store**: Efficient vector storage and retrieval
- **PDF Processing**: Automatically processes Kotlin documentation PDFs
- **Clean Architecture**: Well-structured codebase with separation of concerns
- **Comprehensive Error Handling**: Robust exception handling with meaningful error messages
- **Validation**: Input validation with detailed error responses
- **Logging**: Structured logging throughout the application
- **Testing**: Unit tests with MockK and JUnit 5

## 🏗️ Architecture

### Core Components

1. **KotlinChatbotService**: Main service handling question processing and answer generation
2. **PdfProcessingService**: Handles PDF document processing and chunking
3. **VectorStoreConfig**: Configuration for Qdrant vector store
4. **InitializationService**: Handles application startup and default document loading
5. **GlobalExceptionHandler**: Centralized error handling

### Data Flow

```
User Question → Controller → ChatbotService → Vector Search → LLM → Response
```

## 🛠️ Prerequisites

- **Java 17+**
- **Kotlin 1.9.25+**
- **Maven 3.8+**
- **OpenAI API Key**
- **Qdrant Vector Database** (local or cloud)

## ⚙️ Configuration

### Environment Variables

```bash
# Required
export OPENAI_API_KEY="your-openai-api-key"

# Optional (with defaults)
export QDRANT_HOST="localhost"
export QDRANT_PORT="6334"
export QDRANT_API_KEY=""
export QDRANT_COLLECTION_NAME="kotlin_docs"
export QDRANT_USE_TLS="false"
```

### Application Properties

Key configuration options in `application.properties`:

```properties
# Chatbot Configuration
app.chatbot.max-context-documents=5

# PDF Processing
app.pdf.processing.chunk-size=800
app.pdf.processing.chunk-overlap=100
app.pdf.processing.batch-size=50

# Initialization
app.initialization.auto-load-default-pdf=false
app.initialization.default-pdf-path=classpath:kotlin-docs.pdf
```

## 🚀 Getting Started

### 1. Clone and Setup

```bash
git clone <repository-url>
cd kotlin-ai-chatbot
```

### 2. Set Environment Variables

```bash
export OPENAI_API_KEY="your-openai-api-key"
```

### 3. Start Qdrant (using Docker)

```bash
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Endpoints

### Chat Endpoints

#### POST /api/kotlin-chatbot/ask
Ask a question about Kotlin programming.

**Request Body:**
```json
{
  "question": "What are Kotlin data classes?"
}
```

**Response:**
```json
{
  "answer": "Kotlin data classes are...",
  "confidence": "HIGH",
  "contextDocumentCount": 3,
  "responseTimeMs": 1250,
  "successful": true,
  "errorMessage": null
}
```

#### GET /api/kotlin-chatbot/ask
Alternative GET endpoint for simple questions.

```
GET /api/kotlin-chatbot/ask?question=What%20is%20Kotlin?
```

#### GET /api/kotlin-chatbot/info
Get chatbot information.

**Response:**
```json
{
  "name": "Kotlin Expert Chatbot",
  "version": "1.0.0",
  "description": "A specialized AI chatbot for answering Kotlin programming questions using RAG",
  "usage": "Ask me anything about Kotlin programming - syntax, concepts, best practices, and more!"
}
```

## 🔧 Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=KotlinChatbotServiceTest
```

### Code Structure

```
src/main/kotlin/com/spring/kotlin_ai_chatbot/
├── KotlinAiChatbotApplication.kt     # Main application class
├── controller/
│   └── KotlinChatbotController.kt    # REST API controller
├── service/
│   ├── KotlinChatbotService.kt       # Core chatbot logic
│   ├── PdfProcessingService.kt       # PDF processing
│   └── InitializationService.kt      # App initialization
├── config/
│   └── VectorStoreConfig.kt          # Qdrant configuration
├── dto/
│   └── ChatDTOs.kt                   # Data transfer objects
└── exception/
    └── GlobalExceptionHandler.kt     # Error handling
```

### Key Kotlin Features Used

- **Data Classes**: For DTOs and response objects
- **Extension Functions**: For enhanced readability
- **Null Safety**: Comprehensive null handling
- **Coroutines Ready**: Architecture supports async operations
- **Smart Casts**: Leveraging Kotlin's type inference
- **Default Parameters**: Clean API design
- **Companion Objects**: For constants and factory methods

## 🎯 Usage Examples

### Basic Question
```bash
curl -X POST http://localhost:8080/api/kotlin-chatbot/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is a Kotlin data class?"}'
```

### Complex Question
```bash
curl -X POST http://localhost:8080/api/kotlin-chatbot/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How do I implement coroutines in Kotlin for async programming?"}'
```

### GET Request
```bash
curl "http://localhost:8080/api/kotlin-chatbot/ask?question=What%20are%20Kotlin%20sealed%20classes?"
```

## 🔍 Monitoring and Debugging

### Logging Levels

The application uses structured logging. Key loggers:

- `com.spring.kotlin_ai_chatbot` - Application logs
- `org.springframework.ai` - Spring AI framework logs
- `io.qdrant` - Qdrant client logs

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## 🚨 Error Handling

The application provides comprehensive error handling:

- **Validation Errors**: Input validation with detailed messages
- **Vector Store Errors**: Graceful handling of database issues
- **PDF Processing Errors**: Detailed error reporting for document processing
- **Generic Errors**: Catch-all for unexpected issues

## 🎨 Customization

### Adding New Document Sources

Extend `PdfProcessingService` to support additional document formats:

```kotlin
fun processMarkdownResource(resource: Resource): ProcessingResult {
    // Implementation for Markdown processing
}
```

### Custom Confidence Calculation

Modify the confidence calculation in `KotlinChatbotService`:

```kotlin
private fun calculateConfidence(documents: List<Document>, answer: String): String {
    // Your custom logic here
}
```

### Enhanced Prompts

Customize the system prompt in `KotlinChatbotService`:

```kotlin
private const val KOTLIN_EXPERT_PROMPT = """
    Your enhanced prompt here...
"""
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support

For questions or issues:

1. Check the logs for detailed error messages
2. Ensure all environment variables are set correctly
3. Verify Qdrant is running and accessible
4. Check OpenAI API key validity

---

**Built with ❤️ using Kotlin and Spring Boot**