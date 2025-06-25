package com.spring.kotlin_ai_chatbot_kt_version.config

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
class VectorStoreConfig(
    @Value("\${spring.ai.vectorstore.qdrant.host}")
    private val qdrantHost: String,

    @Value("\${spring.ai.vectorstore.qdrant.port}")
    private val qdrantPort: Int,

    @Value("\${spring.ai.vectorstore.qdrant.api-key}")
    private val qdrantApiKey: String,

    @Value("\${spring.ai.vectorstore.qdrant.collection-name}")
    private val collectionName: String,

    @Value("\${spring.ai.vectorstore.qdrant.use-tls}")
    private val useTls: Boolean
) {

    companion object {
        private val logger = LoggerFactory.getLogger(VectorStoreConfig::class.java)
    }

    @Bean
    @Primary
    fun qdrantClient(): QdrantClient {
        logger.info("Initializing Qdrant client with host: {}, port: {}, TLS: {}",
            qdrantHost, qdrantPort, useTls)

        return try {
            val client = QdrantClient(
                QdrantGrpcClient.newBuilder(qdrantHost, qdrantPort, useTls)
                    .withApiKey(qdrantApiKey)
                    .build()
            )

            val collections = client.listCollectionsAsync(Duration.ofSeconds(10)).get()

            client
        } catch (e: Exception) {
            throw RuntimeException("Failed to connect to Qdrant")
        }
    }

    @Bean
    @Primary
    fun vectorStore(qdrantClient: QdrantClient, embeddingModel: EmbeddingModel): VectorStore {

        return try {
            QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(true)
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize vector store")
        }
    }

}