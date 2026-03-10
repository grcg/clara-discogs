/**
 * Navigation contracts for the discography feature.
 * Defines routes and navigation capabilities for discography-related screens.
 */
package com.carmona.clarachallenge.feature.discography.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions

/**
 * Base route for the discography screen
 */
const val DISCOGRAPHY_ROUTE = "discography"

/**
 * Navigation argument key for artist ID in discography screen
 */
const val DISCOGRAPHY_ARTIST_ID_ARG = "artistId"

/**
 * Full route pattern including the artist ID parameter
 * Example: "discography/12345"
 */
const val DISCOGRAPHY_FULL_ROUTE = "$DISCOGRAPHY_ROUTE/{$DISCOGRAPHY_ARTIST_ID_ARG}"

/**
 * Interface defining discography feature navigation capabilities.
 * Implementations should provide type-safe navigation methods for the discography feature.
 */
interface DiscographyNavigation {
    /**
     * Navigates to the discography screen for a specific artist.
     *
     * @param navController The navigation controller to use for navigation
     * @param artistId The unique identifier of the artist whose discography to display
     * @param navOptions Optional navigation options (animations, pop behavior, etc.)
     */
    fun navigateToDiscography(
        navController: NavController,
        artistId: String,
        navOptions: NavOptions? = null
    )

    /**
     * Adds the discography screen to the navigation graph.
     *
     * @param navGraphBuilder The navigation graph builder
     * @param onBackPressed Callback for when the user presses back
     */
    fun discographyScreen(
        navGraphBuilder: NavGraphBuilder,
        onBackPressed: () -> Unit
    )
}