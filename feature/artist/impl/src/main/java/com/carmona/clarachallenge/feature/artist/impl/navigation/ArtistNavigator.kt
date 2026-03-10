/**
 * Navigator implementation for the artist feature.
 * Provides type-safe navigation methods for the artist screen.
 *
 * This class is injectable via Hilt and should be scoped as a singleton
 * to ensure consistent navigation state.
 */
package com.carmona.clarachallenge.feature.artist.impl.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carmona.clarachallenge.feature.artist.api.ArtistsNavigation
import com.carmona.clarachallenge.feature.artist.api.ARTIST_ROUTE
import com.carmona.clarachallenge.feature.artist.api.ARTIST_ID_ARG
import com.carmona.clarachallenge.feature.artist.api.ARTIST_FULL_ROUTE
import com.carmona.clarachallenge.feature.artist.impl.ui.ArtistScreen
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigator implementation for the artist feature.
 * Provides type-safe navigation methods for the artist screen.
 *
 * This class is injectable via Hilt and scoped as a singleton
 * to ensure consistent navigation state across the app.
 *
 * @constructor Creates an instance of ArtistNavigator
 */
@Singleton
class ArtistNavigator @Inject constructor() : ArtistsNavigation {

    /**
     * Navigates to the artist detail screen by constructing the route with the artist ID.
     *
     * @param navController The navigation controller to use for navigation
     * @param artistId The unique identifier of the artist to display
     * @param navOptions Optional navigation configuration (animations, pop behavior, etc.)
     */
    override fun navigateToArtist(
        navController: NavController,
        artistId: String,
        navOptions: NavOptions?
    ) {
        navController.navigate("$ARTIST_ROUTE/$artistId", navOptions)
    }

    /**
     * Adds the artist composable screen to the navigation graph.
     * The screen expects a String argument for the artist ID.
     *
     * @param navGraphBuilder The navigation graph builder
     * @param onBackPressed Callback invoked when the user presses the back button
     * @param onNavigateToDiscography Callback invoked when user wants to see full discography
     */
    override fun artistScreen(
        navGraphBuilder: NavGraphBuilder,
        onBackPressed: () -> Unit,
        onNavigateToDiscography: (String) -> Unit
    ) {
        navGraphBuilder.composable(
            route = ARTIST_FULL_ROUTE,
            arguments = listOf(
                navArgument(ARTIST_ID_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString(ARTIST_ID_ARG)
                ?: return@composable

            ArtistScreen(
                artistId = artistId,
                onBackPressed = onBackPressed,
                onNavigateToDiscography = onNavigateToDiscography
            )
        }
    }
}