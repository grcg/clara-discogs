/**
 * Navigation contracts for the artists feature.
 * Defines routes and navigation capabilities for artist-related screens.
 */
package com.carmona.clarachallenge.feature.artist.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions

/**
 * Base route for the artist screen
 */
const val ARTIST_ROUTE = "artist"

/**
 * Navigation argument key for artist ID
 */
const val ARTIST_ID_ARG = "artistId"

/**
 * Full route pattern including the artist ID parameter
 * Example: "artist/12345"
 */
const val ARTIST_FULL_ROUTE = "$ARTIST_ROUTE/{$ARTIST_ID_ARG}"

/**
 * Interface defining artists feature navigation capabilities.
 * Implementations should provide type-safe navigation methods for the artist feature.
 */
interface ArtistsNavigation {
    /**
     * Navigates to the artist detail screen.
     *
     * @param navController The navigation controller to use for navigation
     * @param artistId The unique identifier of the artist to display
     * @param navOptions Optional navigation options (animations, pop behavior, etc.)
     */
    fun navigateToArtist(
        navController: NavController,
        artistId: String,
        navOptions: NavOptions? = null
    )

    /**
     * Adds the artist screen to the navigation graph.
     *
     * @param navGraphBuilder The navigation graph builder
     * @param onBackPressed Callback for when the user presses back
     * @param onNavigateToDiscography Callback for navigating to the discography screen
     */
    fun artistScreen(
        navGraphBuilder: NavGraphBuilder,
        onBackPressed: () -> Unit,
        onNavigateToDiscography: (String) -> Unit
    )
}