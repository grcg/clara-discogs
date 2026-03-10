/**
 * Extension functions for working with [Result] sealed class.
 * Provides utilities for Flow transformation and Result mapping.
 */
package com.carmona.clarachallenge.core.common.extensions

import com.carmona.clarachallenge.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Converts a Flow of type T to a Flow of [Result&lt;T&gt;] by mapping success values and catching errors.
 *
 * @return Flow that emits [Result.Success] for each value and [Result.Error] for any exceptions
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>> { Result.Success(it) }
    .catch { emit(Result.Error(it.message ?: "Unknown error")) }

/**
 * Transforms the success value of a Result using the provided transform function.
 *
 * @param transform Function to apply to the success value
 * @return A new Result with transformed success value, or the original error/loading state
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> Result.Error(message, Exception())
    Result.Loading -> Result.Loading
}

/**
 * Checks if the Result is a success.
 *
 * @return true if this is a [Result.Success], false otherwise
 */
fun Result<*>.isSuccess(): Boolean = this is Result.Success

/**
 * Checks if the Result is an error.
 *
 * @return true if this is a [Result.Error], false otherwise
 */
fun Result<*>.isError(): Boolean = this is Result.Error

/**
 * Checks if the Result is loading.
 *
 * @return true if this is [Result.Loading], false otherwise
 */
fun Result<*>.isLoading(): Boolean = this == Result.Loading