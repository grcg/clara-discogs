/**
 * Search Feature Navigator
 *
 * This file contains the navigation implementation for the search feature.
 * It follows the same pattern as ArtistNavigator and DiscographyNavigator,
 * providing injectable navigation methods that can be used from AppNavGraph.
 *
 * The navigator pattern provides type-safe navigation by encapsulating
 * route construction and screen composition logic.
 */
package com.carmona.clarachallenge.feature.search.impl.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.carmona.clarachallenge.feature.search.api.SearchNavigation
import com.carmona.clarachallenge.feature.search.api.SEARCH_ROUTE
import com.carmona.clarachallenge.feature.search.impl.ui.SearchScreen
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigator implementation for the search feature.
 *
 * This class provides type-safe navigation methods for the search screen.
 * It is injectable via Hilt and scoped as a singleton to ensure consistent
 * navigation state throughout the app.
 *
 * The navigator pattern offers several advantages:
 * 1. Centralizes navigation logic in one place
 * 2. Provides type-safe methods with clear parameters
 * 3. Makes navigation testable by allowing mock implementations
 * 4. Keeps navigation code independent of the UI implementation
 *
 * @property onArtistClick Callback that will be set by AppNavGraph to handle
 *                         navigation from search to artist screen
 */
@Singleton
class SearchNavigator @Inject constructor() : SearchNavigation {

    /**
     * Navigates to the search screen.
     *
     * This method is used to navigate to the search screen from any part of the app.
     * The search screen serves as the main entry point and allows users to search
     * for artists.
     *
     * @param navController The NavController used to perform the navigation
     * @param navOptions Optional navigation options (animations, pop behavior, etc.)
     *
     * Usage in AppNavGraph:
     * ```
     * searchNavigator.navigateToSearch(navController)
     * ```
     */
    override fun navigateToSearch(
        navController: NavController,
        navOptions: NavOptions?
    ) {
        navController.navigate(SEARCH_ROUTE, navOptions)
    }

    /**
     * Adds the search screen to a navigation graph.
     *
     * This method defines how the search screen is composed within a NavGraph.
     * It creates a composable destination that renders the SearchScreen component
     * and handles any navigation arguments.
     *
     * The search screen is the entry point of the app and does not require
     * any navigation arguments. When an artist is selected, it triggers the
     * provided callback which should navigate to the artist details screen.
     *
     * @param navGraphBuilder The NavGraphBuilder used to add the destination
     * @param onArtistClick Callback invoked when an artist is selected, passing the artist ID
     *                      This callback should be implemented in AppNavGraph to
     *                      handle navigation to the artist screen
     *
     * Usage in AppNavGraph:
     * ```
     * searchNavigator.searchScreen(
     *     navGraphBuilder = this,
     *     onArtistClick = { artistId ->
     *         artistNavigator.navigateToArtist(navController, artistId)
     *     }
     * )
     * ```
     */
    override fun searchScreen(
        navGraphBuilder: NavGraphBuilder,
        onArtistClick: (String) -> Unit
    ) {
        navGraphBuilder.composable(
            route = SEARCH_ROUTE
        ) {
            // Render the search screen with the provided callback
            // The SearchScreen component handles user input and search logic internally
            SearchScreen(
                onArtistClick = onArtistClick
            )
        }
    }
}