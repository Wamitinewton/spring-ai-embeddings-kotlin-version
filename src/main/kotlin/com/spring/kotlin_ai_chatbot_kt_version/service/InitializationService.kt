package com.spring.kotlin_ai_chatbot_kt_version.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class InitializationService(
    private val pdfProcessingService: PdfProcessingService,
    private val resourceLoader: ResourceLoader,
    @Value("\${app.initialization.default-pdf-path:classpath:kotlin-docs.pdf}")
    private val defaultPdfPath: String,
    @Value("\${app.initialization.auto-load-default-pdf:false}")
    private val autoLoadDefaultPdf: Boolean
) : CommandLineRunner {

    companion object {
        private val logger = LoggerFactory.getLogger(InitializationService::class.java)
    }

    override fun run(vararg args: String) {
        if (autoLoadDefaultPdf) {
            loadDefaultKotlinDocumentation()
        } else {
            logger.info("Auto-loading of default PDF is disabled. Use the upload endpoint to add Kotlin documentation.")
            logger.info("To enable auto-loading, set app.initialization.auto-load-default-pdf=true")
        }
    }

    private fun loadDefaultKotlinDocumentation() {
        try {
            logger.info("Attempting to load default Kotlin documentation from: {}", defaultPdfPath)

            val pdfResource = resourceLoader.getResource(defaultPdfPath)

            if (!pdfResource.exists()) {
                logger.warn("Default Kotlin PDF not found at: {}. Please upload a Kotlin documentation PDF manually.", defaultPdfPath)
                return
            }

            logger.info("Found default Kotlin PDF. Starting processing...")
            val result = pdfProcessingService.processPdfResource(pdfResource)

            if (result.successful) {
                logger.info("Successfully loaded default Kotlin documentation! " +
                        "Documents: {}, Chunks: {}, Time: {}ms",
                    result.documentsProcessed,
                    result.chunksCreated,
                    result.processingTimeMs)
            } else {
                logger.error("Failed to process default Kotlin documentation: {}", result.errorMessage)
            }

        } catch (e: Exception) {
            logger.error("Error loading default Kotlin documentation", e)
        }
    }

    fun reloadDefaultDocumentation() {
        logger.info("Manually reloading default Kotlin documentation...")
        loadDefaultKotlinDocumentation()
    }
}