/**
 * Events that can be triggered from the artist UI.
 * Represents all possible user interactions with the artist screen.
 */
package com.carmona.clarachallenge.feature.artist.impl.ui

import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.ArtistDetails
import com.carmona.clarachallenge.core.model.Release

/**
 * Sealed class representing all possible user events in the artist screen.
 * Each event corresponds to a specific user action that the ViewModel should handle.
 */
sealed class ArtistEvent {
    /**
     * Event triggered when the screen needs to load artist data.
     *
     * @param artistId The ID of the artist to load
     */
    data class LoadArtist(val artistId: String) : ArtistEvent()

    /**
     * Event triggered when the user requests a refresh of the current artist data.
     */
    data object RefreshArtist : ArtistEvent()

    /**
     * Event triggered when the user wants to navigate to the full discography.
     *
     * @param artistId The ID of the artist whose discography to view
     */
    data class NavigateToDiscography(val artistId: String) : ArtistEvent()

    /**
     * Event triggered when the user wants to navigate back to the previous screen.
     */
    data object NavigateBack : ArtistEvent()

    /**
     * Event triggered when an error has been displayed to the user and can be cleared.
     *
     * @param errorId Unique identifier of the error being dismissed
     */
    data class OnErrorShown(val errorId: Long) : ArtistEvent()

    /**
     * Event triggered when the user requests to load more releases (pagination).
     *
     * @param page The page number to load (starts at 1)
     */
    data class LoadMoreReleases(val page: Int) : ArtistEvent()
}

/**
 * UI state for the artist screen.
 * Represents all the data needed to render the artist UI.
 *
 * @param isLoading Whether the initial data is being loaded
 * @param isLoadingMore Whether additional pages are being loaded
 * @param artistDetails Detailed artist information
 * @param artist Basic artist information
 * @param releases List of releases loaded so far
 * @param currentPage Current page number for pagination
 * @param hasMorePages Whether there are more releases to load
 * @param totalReleases Total number of releases available
 * @param bio Artist biography text
 * @param errorMessages Map of error IDs to error messages for active errors
 */
data class ArtistState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val artistDetails: ArtistDetails? = null,
    val artist: Artist? = null,
    val releases: List<Release> = emptyList(),
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val totalReleases: Int = 0,
    val bio: String? = null,
    val errorMessages: Map<Long, String> = emptyMap()
)