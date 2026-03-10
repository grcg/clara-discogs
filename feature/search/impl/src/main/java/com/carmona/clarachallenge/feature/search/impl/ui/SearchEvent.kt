/**
 * Events that can be triggered from the search UI.
 * Represents all possible user interactions with the search screen.
 */
package com.carmona.clarachallenge.feature.search.impl.ui

/**
 * Sealed class representing all possible user events in the search screen.
 * Each event corresponds to a specific user action that the ViewModel should handle.
 */
sealed class SearchEvent {
    /**
     * Event triggered when the user types in the search field.
     *
     * @param query The current search query text
     */
    data class SearchQueryChanged(val query: String) : SearchEvent()

    /**
     * Event triggered when the user submits a search.
     *
     * @param query The search query to execute
     */
    data class PerformSearch(val query: String) : SearchEvent()

    /**
     * Event triggered when the user scrolls to load more results.
     */
    object LoadMoreResults : SearchEvent()

    /**
     * Event triggered when the user taps on an artist in the results.
     *
     * @param artistId The ID of the selected artist (as String for navigation)
     */
    data class OnArtistClick(val artistId: String) : SearchEvent()

    /**
     * Event triggered when the user clears the search.
     */
    object ClearSearch : SearchEvent()

    /**
     * Event triggered when an error has been displayed and can be cleared.
     */
    object ErrorShown : SearchEvent()
}