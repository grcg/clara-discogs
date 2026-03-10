package com.carmona.clarachallenge.feature.artist.impl.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carmona.clarachallenge.core.domain.usecase.GetArtistDetailsUseCase
import com.carmona.clarachallenge.core.domain.usecase.GetArtistReleasesUseCase
import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getArtistDetailsUseCase: GetArtistDetailsUseCase,
    private val getArtistReleasesUseCase: GetArtistReleasesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    // Get artistId from navigation arguments - expects String from the API module
    private val artistId: String = savedStateHandle.get<String>("artistId") ?: ""

    init {
        Timber.d("ArtistViewModel initialized with artistId: $artistId")
        if (artistId.isNotEmpty()) {
            handleEvent(ArtistEvent.LoadArtist(artistId))
        } else {
            Timber.e("ArtistViewModel: artistId is empty!")
        }
    }

    fun handleEvent(event: ArtistEvent) {
        when (event) {
            is ArtistEvent.LoadArtist -> loadArtist(event.artistId)
            ArtistEvent.RefreshArtist -> refreshArtist()
            is ArtistEvent.NavigateToDiscography -> trackNavigationToDiscography(event.artistId)
            ArtistEvent.NavigateBack -> trackBackNavigation()
            is ArtistEvent.OnErrorShown -> dismissError(event.errorId)
            is ArtistEvent.LoadMoreReleases -> loadMoreReleases(event.page)
        }
    }

    private fun loadArtist(artistId: String) {
        Timber.d("loadArtist called with artistId: $artistId")
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, errorMessages = emptyMap())
            }

            val detailsResult = getArtistDetailsUseCase(artistId)
            Timber.d("Artist details result: $detailsResult")

            when (detailsResult) {
                is Result.Success -> {
                    Timber.d("Artist details success: ${detailsResult.data}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            artistDetails = detailsResult.data,
                            artist = Artist(
                                id = detailsResult.data.id,
                                name = detailsResult.data.name,
                                bio = detailsResult.data.profile,
                                thumbnailUrl = detailsResult.data.thumbnailUrl,
                                yearFormed = detailsResult.data.yearFormed,
                                genres = detailsResult.data.genres
                            )
                        )
                    }
                    // After loading artist details, load first page of releases
                    loadReleases(artistId, page = 1)
                }
                is Result.Error -> {
                    Timber.e("Artist details error: ${detailsResult.message}")
                    _state.update { it.copy(isLoading = false) }
                    handleError(detailsResult.message)
                }
                is Result.Loading -> {
                    Timber.d("Artist details loading...")
                }
            }
        }
    }

    private fun loadReleases(artistId: String, page: Int) {
        Timber.d("loadReleases called for artistId: $artistId, page: $page")
        viewModelScope.launch {
            _state.update {
                if (page == 1) {
                    it.copy(isLoading = true)
                } else {
                    it.copy(isLoadingMore = true)
                }
            }

            val releasesResult = getArtistReleasesUseCase(artistId, page, emptyMap())
            Timber.d("Releases result for page $page: $releasesResult")

            when (releasesResult) {
                is Result.Success -> {
                    val paginatedResult = releasesResult.data
                    Timber.d("Releases success: ${paginatedResult.data.size} items, hasMorePages: ${paginatedResult.hasMorePages}")

                    _state.update { currentState ->
                        val newReleases = if (page == 1) {
                            paginatedResult.data
                        } else {
                            currentState.releases + paginatedResult.data
                        }

                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            releases = newReleases,
                            currentPage = page,
                            hasMorePages = paginatedResult.hasMorePages,
                            totalReleases = paginatedResult.totalItems
                        )
                    }
                    Timber.d("State updated with ${_state.value.releases.size} releases")
                }
                is Result.Error -> {
                    Timber.e("Releases error: ${releasesResult.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false
                        )
                    }
                    handleError(releasesResult.message)
                }
                is Result.Loading -> {
                    Timber.d("Releases loading...")
                }
            }
        }
    }

    private fun loadMoreReleases(page: Int) {
        if (artistId.isNotEmpty() && _state.value.hasMorePages && !_state.value.isLoadingMore) {
            Timber.d("Loading more releases, page: $page")
            loadReleases(artistId, page)
        }
    }

    private fun refreshArtist() {
        Timber.d("Refreshing artist data")
        if (artistId.isNotEmpty()) {
            loadArtist(artistId)
        }
    }

    private fun handleError(errorMessage: String) {
        val errorId = System.currentTimeMillis()
        Timber.e("Error: $errorMessage")
        _state.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessages = it.errorMessages + (errorId to errorMessage)
            )
        }
    }

    private fun dismissError(errorId: Long) {
        _state.update {
            it.copy(errorMessages = it.errorMessages - errorId)
        }
    }

    private fun trackNavigationToDiscography(artistId: String) {
        Timber.d("Navigating to discography for artist: $artistId")
    }

    private fun trackBackNavigation() {
        Timber.d("Navigating back from artist screen")
    }
}