/**
 * Network Dependency Injection Module
 *
 * This Hilt module provides network-related dependencies for the app.
 * It configures and provides a singleton HttpClient instance with all
 * necessary plugins and settings for communicating with the Discogs API.
 *
 * The module uses Ktor as the HTTP client and includes:
 * - Content negotiation for JSON serialization
 * - Timeout configuration
 * - Logging for debugging
 * - Default request headers (including Authorization)
 *
 * @see DiscogsApi for the API interface
 * @see DiscogsApiImpl for the implementation
 */
package com.carmona.clarachallenge.core.network.di

import com.carmona.clarachallenge.core.network.BuildConfig
import com.carmona.clarachallenge.core.network.DiscogsApiImpl
import com.carmona.clarachallenge.core.network.api.DiscogsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies.
 * All providers are scoped as [Singleton] to ensure a single instance
 * is used throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured singleton HttpClient instance.
     *
     * The client is configured with:
     * - Android engine for platform-specific networking
     * - JSON content negotiation with lenient settings
     * - Timeout configuration (30 seconds for all timeouts)
     * - Request/response logging for debugging
     * - Default Authorization header with Discogs token
     *
     * @return A fully configured HttpClient instance
     */
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {

            /**
             * Content Negotiation plugin for JSON serialization/deserialization.
             * Configures Kotlinx Serialization with lenient settings to handle
             * API responses gracefully.
             */
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true  // Ignore fields not defined in our models
                    isLenient = true          // Be forgiving with malformed JSON
                    prettyPrint = false       // Don't pretty print (reduces response size)
                })
            }

            /**
             * Timeout configuration to prevent hanging requests.
             * All timeouts set to 30 seconds.
             */
            install(HttpTimeout) {
                requestTimeoutMillis = 30000   // Timeout for the entire request
                connectTimeoutMillis = 30000    // Timeout for establishing connection
                socketTimeoutMillis = 30000     // Timeout for waiting for data
            }

            /**
             * Logging plugin for debugging network calls.
             * Logs headers and basic request/response info.
             * In production, consider setting level to NONE.
             */
            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : Logger {
                    override fun log(message: String) {
                        println("HTTP Client: $message")
                    }
                }
            }

            /**
             * Default request configuration applied to every request.
             * Sets common headers including the Authorization token.
             *
             * IMPORTANT: The Discogs token is read from BuildConfig.
             * Make sure to add your token in gradle.properties or
             * as a local property to keep it secure.
             */
            install(DefaultRequest) {
                // User-Agent is required by Discogs API
                header(HttpHeaders.UserAgent, "ClaraChallenge/1.0")

                // Authorization header with Discogs token
                // The token is read from BuildConfig.DISCOGS_TOKEN which should be
                // configured in your gradle.properties or local.properties
                header(
                    HttpHeaders.Authorization,
                    "Discogs token=${BuildConfig.DISCOGS_TOKEN}"
                )
            }
        }
    }

    /**
     * Provides the DiscogsApi implementation.
     *
     * @param client The configured HttpClient instance
     * @return An instance of DiscogsApiImpl that implements the DiscogsApi interface
     */
    @Provides
    @Singleton
    fun provideDiscogsApi(client: HttpClient): DiscogsApi {
        return DiscogsApiImpl(client)
    }
}