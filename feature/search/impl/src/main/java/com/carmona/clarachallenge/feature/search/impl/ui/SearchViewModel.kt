package com.carmona.clarachallenge.feature.search.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carmona.clarachallenge.core.domain.usecase.SearchArtistsUseCase
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
 * Search ViewModel
 *
 * This ViewModel manages the UI state and business logic for the artist search feature.
 * It handles user input debouncing, search execution, pagination, error handling,
 * and recent searches management.
 *
 * The ViewModel follows the MVVM pattern and exposes an immutable [StateFlow] for UI observation.
 * All state updates are done through the private `_state` MutableStateFlow to ensure
 * thread safety and consistency.
 *
 * Key features:
 * - Debounced search input to prevent excessive API calls
 * - Immediate search execution on submit button
 * - Pagination with "load more" functionality
 * - Error handling with dismissible error messages
 * - Recent searches tracking with 10-item limit
 *
 * @param searchArtistsUseCase The use case for searching artists, injected via Hilt
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchArtistsUseCase: SearchArtistsUseCase
) : ViewModel() {

    /**
     * Internal mutable state flow that holds the current UI state.
     * Only this ViewModel should update it to ensure state consistency.
     * Updates are performed using the thread-safe `update` function.
     */
    private val _state = MutableStateFlow(SearchState())

    /**
     * External immutable state flow for UI observation.
     * The UI collects from this flow to recompose when state changes.
     * This ensures the UI always displays the latest state without
     * being able to modify it directly.
     */
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /**
     * Job for debouncing search queries.
     * Cancels the previous debounce job when the user types a new character
     * to prevent unnecessary API calls. The actual search is only triggered
     * after the user stops typing for 500ms.
     */
    private var searchDebounceJob: Job? = null

    /**
     * Job for the currently executing search or load more operation.
     * Tracks the active coroutine to prevent multiple simultaneous operations
     * and to allow cancellation when a new operation starts.
     */
    private var currentLoadJob: Job? = null

    /**
     * Updates the search query and triggers a debounced search.
     *
     * This function is called whenever the user types in the search bar.
     * It updates the query in state immediately but waits 500ms before
     * executing the actual search to avoid excessive API calls while typing.
     *
     * If the query becomes empty, it clears the results and resets pagination.
     *
     * @param query The new search query entered by the user
     */
    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }

        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(500)
            if (query.isNotBlank()) {
                performSearch(query, page = 1)
            } else {
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
     * the search immediately. It also adds the query to recent searches
     * and ensures the query is properly set in state before searching.
     *
     * Recent searches are maintained as a list of unique queries,
     * with the most recent at the beginning, limited to 10 items.
     *
     * @param query The search query to execute
     */
    fun onSearch(query: String) {
        searchDebounceJob?.cancel()
        if (query.isNotBlank()) {
            _state.update { it.copy(query = query) }

            performSearch(query, page = 1)

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
     * It checks preconditions before loading:
     * - There are more results available (`hasMoreResults` is true)
     * - No load operation is currently in progress
     * - The query is not blank
     *
     * Results from the next page are appended to the existing results list.
     * The function includes debug logging to help trace pagination issues.
     */
    fun loadMoreResults() {
        val currentState = _state.value

        if (!currentState.hasMoreResults) {
            return
        }
        if (currentLoadJob?.isActive == true) {
            return
        }
        if (currentState.query.isBlank()) {
            return
        }

        val nextPage = currentState.currentPage + 1
        val query = currentState.query

        currentLoadJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            val result = searchArtistsUseCase(query, nextPage)

            when (result) {
                is Result.Success -> {
                    val paginatedResult = result.data
                    _state.update { state ->
                        val newResults = state.results + paginatedResult.data
                        state.copy(
                            isLoadingMore = false,
                            results = newResults,
                            currentPage = paginatedResult.page,
                            hasMoreResults = paginatedResult.hasMorePages,
                            totalResults = paginatedResult.totalItems,
                            errorMessages = emptyMap()
                        )
                    }
                }
                is Result.Error -> {
                    val errorId = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessages = it.errorMessages + (errorId to result.message)
                        )
                    }
                }
                is Result.Loading -> {}
            }
            currentLoadJob = null
        }
    }

    /**
     * Performs the actual search operation.
     *
     * This private function contains the core search logic:
     * 1. Cancels any ongoing load operation
     * 2. Sets loading state and clears previous results
     * 3. Executes the use case
     * 4. Processes the result (success or error)
     * 5. Updates the UI state accordingly
     *
     * Note: The query is preserved in state and not cleared during loading.
     *
     * @param query The search query to execute
     * @param page The page number to load (starts at 1)
     */
    private fun performSearch(query: String, page: Int) {
        currentLoadJob?.cancel()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    results = emptyList(),
                    errorMessages = emptyMap(),
                    isLoadingMore = false
                )
            }

            val result = searchArtistsUseCase(query, page)

            when (result) {
                is Result.Success -> {
                    val paginatedResult = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            results = paginatedResult.data,
                            currentPage = paginatedResult.page,
                            hasMoreResults = paginatedResult.hasMorePages,
                            totalResults = paginatedResult.totalItems,
                            errorMessages = emptyMap()
                        )
                    }
                }

                is Result.Error -> {
                    val errorId = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessages = it.errorMessages + (errorId to result.message)
                        )
                    }
                }

                is Result.Loading -> {}
            }
        }
    }

    /**
     * Retries the last failed search.
     *
     * This function is called when the user taps the retry button
     * after an error occurs. It re-executes the search with the current query
     * and page 1, effectively starting a fresh search.
     */
    fun onRetry() {
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery, page = 1)
        }
    }

    /**
     * Dismisses an error message.
     *
     * This function is called when an error snackbar is dismissed,
     * either by the user or automatically. It removes the error from state
     * using its unique ID, allowing multiple errors to be managed independently.
     *
     * @param errorId The unique ID of the error to dismiss
     */
    fun onErrorDismissed(errorId: Long) {
        _state.update {
            it.copy(errorMessages = it.errorMessages - errorId)
        }
    }
}