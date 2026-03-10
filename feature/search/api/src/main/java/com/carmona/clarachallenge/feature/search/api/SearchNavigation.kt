/**
 * Search Feature Navigation API
 *
 * This interface defines the navigation contract for the search feature.
 * It provides type-safe methods for navigating to and composing the search screen.
 *
 * The implementation is provided by [SearchNavigator] in the impl module.
 * This separation allows the API module to remain independent of the implementation.
 */
package com.carmona.clarachallenge.feature.search.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions

/**
 * The base route for the search screen.
 * This constant is used as the start destination in the main navigation graph.
 */
const val SEARCH_ROUTE = "search"

/**
 * Interface defining navigation operations for the search feature.
 *
 * Implementing this interface allows for:
 * - Type-safe navigation to the search screen
 * - Adding the search screen to navigation graphs
 * - Testability through mock implementations
 */
interface SearchNavigation {

    /**
     * Navigates to the search screen.
     *
     * @param navController The NavController used to perform the navigation
     * @param navOptions Optional navigation configuration
     */
    fun navigateToSearch(
        navController: NavController,
        navOptions: NavOptions? = null
    )

    /**
     * Adds the search screen to a navigation graph.
     *
     * @param navGraphBuilder The NavGraphBuilder used to add the destination
     * @param onArtistClick Callback when an artist is selected
     */
    fun searchScreen(
        navGraphBuilder: NavGraphBuilder,
        onArtistClick: (String) -> Unit
    )
}

/**
 * Type alias for the search screen composable function.
 * Takes an onArtistClick callback and renders the search UI.
 */
typealias SearchRouteComposable = @Composable (onArtistClick: (String) -> Unit) -> Unit

/**
 * Provider pattern to connect the API module with the IMPL module.
 *
 * This object acts as a bridge between the API contract and the actual implementation.
 * The IMPL module sets the [SearchRoute] composable at app startup (via Hilt),
 * allowing the API module to reference it without a direct dependency.
 *
 * This pattern maintains clean architecture where:
 * - API module defines the contract (what the feature does)
 * - IMPL module provides the implementation (how it does it)
 * - The provider connects them at runtime
 */
object SearchScreenProvider {

    /**
     * The actual composable function that renders the search screen.
     */
    lateinit var SearchRoute: SearchRouteComposable
}