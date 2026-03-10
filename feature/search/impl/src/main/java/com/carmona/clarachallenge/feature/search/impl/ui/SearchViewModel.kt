/**
 * Search ViewModel
 *
 * This ViewModel manages the UI state and business logic for the search screen.
 * It handles user interactions, debounced search, pagination, and error handling.
 *
 * The ViewModel follows the MVVM pattern and is injected via Hilt.
 */
package com.carmona.clarachallenge.feature.search.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carmona.clarachallenge.core.domain.usecase.SearchArtistsUseCase
import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the search screen.
 *
 * This class handles all business logic for the search feature including:
 * - Debounced search when user types
 * - Pagination (loading more results)
 * - Error handling and recovery
 * - Recent searches management
 *
 * @param searchArtistsUseCase The use case for executing artist searches
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchArtistsUseCase: SearchArtistsUseCase
) : ViewModel() {

    /**
     * Internal mutable state flow that holds the current UI state.
     * Only this ViewModel can update it.
     */
    private val _state = MutableStateFlow(SearchState())

    /**
     * External immutable state flow for UI observation.
     * The UI collects from this flow to recompose when state changes.
     */
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /**
     * Job for debouncing search queries.
     * Cancels previous search when user types quickly.
     */
    private var searchDebounceJob: Job? = null

    /**
     * Updates the search query and triggers a debounced search.
     *
     * This function is called whenever the user types in the search bar.
     * It updates the query in state and starts a debounce timer.
     * If the user stops typing for 500ms, it triggers the actual search.
     *
     * @param query The new search query entered by the user
     */
    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }

        // Cancel previous debounce job to prevent unnecessary searches
        searchDebounceJob?.cancel()

        // Start new debounced search
        searchDebounceJob = viewModelScope.launch {
            delay(500) // Wait 500ms after user stops typing
            if (query.isNotBlank()) {
                performSearch(query, page = 1, isNewSearch = true)
            } else {
                // Clear results when query is empty
                _state.update {
                    it.copy(
                        results = emptyList(),
                        hasMoreResults = false,
                        currentPage = 1,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    /**
     * Performs an immediate search (when user presses search button).
     *
     * This function cancels any pending debounced search and executes
     * the search immediately. It also adds the query to recent searches.
     *
     * @param query The search query to execute
     */
    fun onSearch(query: String) {
        searchDebounceJob?.cancel()
        if (query.isNotBlank()) {
            performSearch(query, page = 1, isNewSearch = true)

            // Add to recent searches (keeping last 10 unique searches)
            _state.update { currentState ->
                val updatedRecents = listOf(query) +
                        currentState.recentSearches.filter { it != query }.take(9)
                currentState.copy(recentSearches = updatedRecents)
            }
        }
    }

    /**
     * Loads more results for pagination.
     *
     * This function is called when the user scrolls to the bottom of the list.
     * It checks if more results are available and not already loading,
     * then loads the next page.
     */
    fun loadMoreResults() {
        val currentState = _state.value
        if (currentState.hasMoreResults && !currentState.isLoadingMore && currentState.query.isNotBlank()) {
            performSearch(
                query = currentState.query,
                page = currentState.currentPage + 1,
                isNewSearch = false
            )
        }
    }

    /**
     * Performs the actual search operation.
     *
     * This private function handles the core search logic:
     * - Sets loading states appropriately
     * - Executes the use case
     * - Processes the result (success or error)
     * - Updates the UI state accordingly
     *
     * @param query The search query
     * @param page The page number to load
     * @param isNewSearch True if this is a new search (replaces results),
     *                    False if loading more (appends results)
     */
    private fun performSearch(query: String, page: Int, isNewSearch: Boolean) {
        viewModelScope.launch {
            // Set appropriate loading state
            _state.update {
                if (isNewSearch) {
                    it.copy(
                        isLoading = true,
                        results = emptyList(),
                        errorMessages = emptyMap(),
                        isLoadingMore = false
                    )
                } else {
                    it.copy(isLoadingMore = true)
                }
            }

            // Execute the search use case
            val result = searchArtistsUseCase(query, page)

            when (result) {
                is Result.Success -> {
                    val paginatedResult = result.data
                    _state.update { currentState ->
                        // Combine results appropriately (replace or append)
                        val newResults = if (isNewSearch) {
                            paginatedResult.data
                        } else {
                            currentState.results + paginatedResult.data
                        }

                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            results = newResults,
                            currentPage = paginatedResult.page,
                            hasMoreResults = paginatedResult.hasMorePages,
                            totalResults = paginatedResult.totalItems
                        )
                    }
                }

                is Result.Error -> {
                    // Generate unique ID for this error
                    val errorId = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessages = it.errorMessages + (errorId to result.message)
                        )
                    }
                }

                is Result.Loading -> {
                    // Loading state already handled, nothing to do here
                }
            }
        }
    }

    /**
     * Retries the last failed search.
     *
     * This function is called when the user taps the retry button
     * after an error occurs. It re-executes the search with the current query.
     */
    fun onRetry() {
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery, page = 1, isNewSearch = true)
        }
    }

    /**
     * Dismisses an error message.
     *
     * This function is called when an error snackbar is dismissed,
     * either by the user or automatically. It removes the error from state.
     *
     * @param errorId The unique ID of the error to dismiss
     */
    fun onErrorDismissed(errorId: Long) {
        _state.update {
            it.copy(errorMessages = it.errorMessages - errorId)
        }
    }
}