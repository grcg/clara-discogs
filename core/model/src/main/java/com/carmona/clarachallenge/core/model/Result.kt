package com.carmona.clarachallenge.core.model

/**
 * Sealed class representing the result of an operation that can succeed, fail, or be in progress.
 *
 * @param T The type of data on success
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}