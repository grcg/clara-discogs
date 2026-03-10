/**
 * Search State Model
 *
 * This file defines the UI state for the search screen.
 * It follows the Unidirectional Data Flow pattern where state is
 * immutable and only updated through the ViewModel.
 */
package com.carmona.clarachallenge.feature.search.impl.ui

import com.carmona.clarachallenge.core.model.Artist

/**
 * Represents the UI state for the search screen.
 *
 * This data class encapsulates all the information needed to render
 * the search UI, including the current query, search results, loading
 * states, pagination info, and error messages.
 *
 * @param query Current search query entered by the user
 * @param results List of artist search results
 * @param isLoading Whether initial search is loading
 * @param isLoadingMore Whether additional pages are loading (pagination)
 * @param hasMoreResults Whether more results are available for pagination
 * @param currentPage Current page number of results
 * @param totalResults Total number of results available
 * @param errorMessages Map of error messages with unique IDs for displaying multiple errors
 * @param recentSearches List of recent search queries for quick access
 */
data class SearchState(
    val query: String = "",
    val results: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreResults: Boolean = false,
    val currentPage: Int = 1,
    val totalResults: Int = 0,
    val errorMessages: Map<Long, String> = emptyMap(),
    val recentSearches: List<String> = emptyList()
)