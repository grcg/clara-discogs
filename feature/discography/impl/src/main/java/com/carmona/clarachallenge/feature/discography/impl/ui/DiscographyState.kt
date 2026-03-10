package com.carmona.clarachallenge.feature.discography.impl.ui

import com.carmona.clarachallenge.core.model.Release

/**
 * UI state for the discography screen
 *
 * @param releases List of releases to display
 * @param isLoading Whether initial data is being loaded
 * @param isLoadingMore Whether additional pages are being loaded
 * @param currentPage Current page number loaded
 * @param hasMorePages Whether there are more pages to load
 * @param artistId ID of the artist whose discography is displayed
 * @param artistName Name of the artist (for display)
 * @param error Error message if any, null if no error
 * @param showFilters Whether to show filter options
 * @param activeFilters Currently applied filters
 */
data class DiscographyState(
    val releases: List<Release> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val artistId: String = "",
    val artistName: String = "",
    val error: String? = null,
    val showFilters: Boolean = false,
    val activeFilters: Map<String, String> = emptyMap()
)