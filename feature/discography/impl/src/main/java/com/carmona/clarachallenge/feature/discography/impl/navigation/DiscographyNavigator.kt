/**
 * Navigator implementation for the discography feature.
 * Provides type-safe navigation methods for the discography screen.
 */
package com.carmona.clarachallenge.feature.discography.impl.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carmona.clarachallenge.feature.discography.api.*
import com.carmona.clarachallenge.feature.discography.impl.ui.DiscographyScreen
import javax.inject.Inject

/**
 * Navigator implementation for the discography feature.
 * Provides type-safe navigation methods for the discography screen.
 *
 * This class is injectable via Hilt and should be scoped as a singleton
 * to ensure consistent navigation state.
 *
 * @constructor Creates an instance of DiscographyNavigator
 */
class DiscographyNavigator @Inject constructor() : DiscographyNavigation {
    /**
     * Navigates to the discography screen by constructing the route with the artist ID.
     *
     * @param navController The navigation controller to use for navigation
     * @param artistId The unique identifier of the artist whose discography to display
     * @param navOptions Optional navigation configuration (animations, pop behavior, etc.)
     */
    override fun navigateToDiscography(
        navController: NavController,
        artistId: String,
        navOptions: NavOptions?
    ) {
        navController.navigate("$DISCOGRAPHY_ROUTE/$artistId", navOptions)
    }

    /**
     * Adds the discography composable screen to the navigation graph.
     * The screen expects a String argument for the artist ID.
     *
     * @param navGraphBuilder The navigation graph builder
     * @param onBackPressed Callback invoked when the user presses the back button
     */
    override fun discographyScreen(
        navGraphBuilder: NavGraphBuilder,
        onBackPressed: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = DISCOGRAPHY_FULL_ROUTE,
            arguments = listOf(
                navArgument(DISCOGRAPHY_ARTIST_ID_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString(DISCOGRAPHY_ARTIST_ID_ARG)
                ?: return@composable

            DiscographyScreen(
                artistId = artistId,
                onBackPressed = onBackPressed
            )
        }
    }
}