/**
 * Network Utilities
 *
 * Helper functions for safe network operations.
 */
package com.carmona.clarachallenge.core.network.api

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

/**
 * Suspends and safely executes a network request, wrapping the result in a [Result] type.
 *
 * This function handles:
 * - HTTP status code validation (only 200 OK is considered success)
 * - JSON parsing errors
 * - Network exceptions
 *
 * @param T The expected response type (must be serializable)
 * @param request The network request block to execute
 * @return [Result] containing the parsed response on success, or an exception on failure
 */
suspend inline fun <reified T> safeRequest(
    crossinline request: suspend () -> HttpResponse
): Result<T> {
    return try {
        val response = request()

        if (response.status == HttpStatusCode.OK) {
            try {
                Result.success(response.body())
            } catch (e: Exception) {
                // JSON parsing error
                Result.failure(NetworkException.ParsingException("Failed to parse response", e))
            }
        } else {
            // HTTP error
            Result.failure(NetworkException.HttpException(response.status.value, response.status.description))
        }
    } catch (e: Exception) {
        // Network error (timeout, no internet, etc.)
        Result.failure(NetworkException.NetworkConnectionException(e))
    }
}

/**
 * Sealed class hierarchy for network-related exceptions.
 * Provides specific error types for better error handling.
 */
sealed class NetworkException : Exception() {

    /**
     * Exception for HTTP errors (non-200 status codes)
     *
     * @param code HTTP status code
     * @param message Status description
     */
    data class HttpException(
        val code: Int,
        val mMessage: String
    ) : NetworkException() {
        override fun toString(): String = "HTTP $code: $mMessage"
    }

    /**
     * Exception for JSON parsing errors
     *
     * @param message Error description
     * @param cause Original exception
     */
    data class ParsingException(
        val mMessage: String,
        val mCause: Throwable
    ) : NetworkException() {
        override fun toString(): String = "Parsing error: $message"
    }

    /**
     * Exception for network connection issues (timeout, no internet, etc.)
     *
     * @param cause Original exception
     */
    data class NetworkConnectionException(
        val mCause: Throwable
    ) : NetworkException() {
        override fun toString(): String = "Network connection failed: ${mCause.message}"
    }
}