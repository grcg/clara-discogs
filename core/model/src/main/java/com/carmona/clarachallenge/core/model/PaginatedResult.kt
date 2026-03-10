package com.carmona.clarachallenge.core.model

/**
 * Generic wrapper for paginated API responses.
 *
 * @param T The type of data being paginated
 * @param data List of items for the current page
 * @param page Current page number (usually 1-based)
 * @param hasMorePages Whether additional pages are available
 * @param totalPages Total number of pages available
 * @param totalItems Total number of items across all pages
 */
data class PaginatedResult<T>(
    val data: List<T>,
    val page: Int,
    val hasMorePages: Boolean,
    val totalPages: Int,
    val totalItems: Int
)