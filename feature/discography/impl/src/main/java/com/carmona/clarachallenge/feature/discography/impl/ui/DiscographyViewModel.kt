/**
 * ViewModel for the discography screen.
 * Manages loading and filtering of artist releases with pagination support.
 */
package com.carmona.clarachallenge.feature.discography.impl.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carmona.clarachallenge.core.model.Result
import com.carmona.clarachallenge.core.model.Release
import com.carmona.clarachallenge.core.domain.usecase.GetArtistDetailsUseCase
import com.carmona.clarachallenge.core.domain.usecase.GetArtistReleasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the discography screen.
 * Handles loading and filtering of artist releases with pagination support.
 *
 * @param getArtistReleasesUseCase Use case for fetching artist releases
 * @param getArtistDetailsUseCase Use case for fetching artist details (for the artist name)
 * @param savedStateHandle For accessing navigation arguments
 */
@HiltViewModel
class DiscographyViewModel @Inject constructor(
    private val getArtistReleasesUseCase: GetArtistReleasesUseCase,
    private val getArtistDetailsUseCase: GetArtistDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DiscographyState())

    /**
     * Public state flow for UI observation.
     * Collect this flow to get updates about the current UI state.
     */
    val state: StateFlow<DiscographyState> = _state.asStateFlow()

    // Track current results for pagination
    private var currentReleases = emptyList<Release>()

    /**
     * Handles user events from the discography screen.
     *
     * @param event The event to handle
     */
    fun handleEvent(event: DiscographyEvent) {
        when (event) {
            is DiscographyEvent.LoadReleases -> {
                loadArtistDetails(event.artistId)
                loadReleases(event.artistId, event.page)
            }

            is DiscographyEvent.LoadMoreReleases -> {
                loadMoreReleases(event.page)
            }

            is DiscographyEvent.ApplyFilters -> {
                applyFilters(event.filters)
            }

            is DiscographyEvent.ToggleFilters -> {
                _state.update { it.copy(showFilters = !it.showFilters) }
            }

            is DiscographyEvent.OnReleaseClick -> {
                // Handle release click - will be passed to navigation
                // Navigation is handled at a higher level
            }

            is DiscographyEvent.ErrorShown -> {
                _state.update { it.copy(error = null) }
            }

            DiscographyEvent.Retry -> {
                val currentState = _state.value
                if (currentState.artistId.isNotBlank()) {
                    loadReleases(currentState.artistId, 1)
                }
            }

            DiscographyEvent.ClearFilters -> {
                _state.update { it.copy(activeFilters = emptyMap()) }
                val currentState = _state.value
                if (currentState.artistId.isNotBlank()) {
                    loadReleases(currentState.artistId, 1, emptyMap())
                }
            }

            else -> {}
        }
    }

    /**
     * Loads artist details to get the artist name for display.
     *
     * @param artistId The artist ID
     */
    private fun loadArtistDetails(artistId: String) {
        viewModelScope.launch {
            when (val result = getArtistDetailsUseCase(artistId)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        artistName = result.data.name,
                        artistId = artistId
                    )}
                }
                is Result.Error -> {
                    // Just use ID as name if details fail
                    _state.update { it.copy(
                        artistName = "Artist",
                        artistId = artistId
                    )}
                }
                Result.Loading -> {}
            }
        }
    }

    /**
     * Loads releases for an artist with pagination and optional filters.
     *
     * @param artistId The artist ID
     * @param page Page number to load (starts at 1)
     * @param filters Optional filter criteria
     */
    private fun loadReleases(
        artistId: String,
        page: Int,
        filters: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = page == 1,
                    isLoadingMore = page > 1,
                    artistId = artistId,
                    activeFilters = filters
                )
            }

            when (val result = getArtistReleasesUseCase(artistId, page, filters)) {
                is Result.Success -> {
                    val paginatedResult = result.data

                    if (page == 1) {
                        currentReleases = paginatedResult.data
                    } else {
                        currentReleases = currentReleases + paginatedResult.data
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            releases = currentReleases,
                            isLoading = false,
                            isLoadingMore = false,
                            currentPage = paginatedResult.page,
                            hasMorePages = paginatedResult.hasMorePages,
                            error = null
                        )
                    }
                }

                is Result.Error -> {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }

                Result.Loading -> {
                    // Already handled in initial state update
                }
            }
        }
    }

    /**
     * Loads the next page of releases if available.
     *
     * @param page Page number to load
     */
    private fun loadMoreReleases(page: Int) {
        val currentState = _state.value
        if (currentState.artistId.isNotBlank() && currentState.hasMorePages && !currentState.isLoadingMore) {
            loadReleases(
                artistId = currentState.artistId,
                page = page,
                filters = currentState.activeFilters
            )
        }
    }

    /**
     * Applies filters and reloads the first page of releases.
     *
     * @param filters Filter criteria to apply
     */
    private fun applyFilters(filters: Map<String, String>) {
        val currentState = _state.value
        if (currentState.artistId.isNotBlank()) {
            loadReleases(currentState.artistId, 1, filters)
        }
    }
}