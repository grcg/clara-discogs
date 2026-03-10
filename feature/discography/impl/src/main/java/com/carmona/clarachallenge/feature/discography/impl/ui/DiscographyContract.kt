package com.carmona.clarachallenge.feature.discography.impl.ui

/**
 * Sealed class representing all possible user events in discography feature
 */
sealed class DiscographyEvent {
    /**
     * Load releases for a specific artist
     * @param artistId ID of the artist
     * @param page Page number to load
     */
    data class LoadReleases(
        val artistId: String,
        val page: Int = 1
    ) : DiscographyEvent()

    /**
     * Load more releases (pagination)
     * @param page Next page number to load
     */
    data class LoadMoreReleases(
        val page: Int
    ) : DiscographyEvent()

    /**
     * Apply filters to the releases list
     * @param filters Map of filter keys to filter values
     */
    data class ApplyFilters(
        val filters: Map<String, String>
    ) : DiscographyEvent()

    /**
     * Toggle filter visibility
     */
    object ToggleFilters : DiscographyEvent()

    /**
     * User clicked on a release
     * @param releaseId ID of the clicked release
     */
    data class OnReleaseClick(
        val releaseId: String
    ) : DiscographyEvent()

    /**
     * Error has been shown/dismissed
     */
    object ErrorShown : DiscographyEvent()

    /**
     * Retry loading after error
     */
    object Retry : DiscographyEvent()

    /**
     * Clear all filters
     */
    object ClearFilters : DiscographyEvent()
}

