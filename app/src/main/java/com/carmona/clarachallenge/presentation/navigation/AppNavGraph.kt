/**
 * Main Application Navigation Graph
 *
 * This file composes all feature navigation graphs into a single app navigation structure.
 * It uses injectable navigators to provide type-safe navigation between features.
 */
package com.carmona.clarachallenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.carmona.clarachallenge.feature.artist.impl.navigation.ArtistNavigator
import com.carmona.clarachallenge.feature.discography.impl.navigation.DiscographyNavigator
import com.carmona.clarachallenge.feature.search.api.SEARCH_ROUTE
import com.carmona.clarachallenge.feature.search.impl.navigation.SearchNavigator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing Hilt dependencies in non-Android components.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavigationEntryPoint {
    fun getArtistNavigator(): ArtistNavigator
    fun getDiscographyNavigator(): DiscographyNavigator
    fun getSearchNavigator(): SearchNavigator
}

/**
 * App-level navigation graph that composes all feature screens.
 *
 * Navigation Structure:
 * - Search Screen (start destination) - Allows users to search for artists
 *   → Artist Screen - Displays detailed artist information
 *     → Discography Screen - Shows artist's releases
 *
 * @param navController The navigation controller, defaults to rememberNavController()
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NavigationEntryPoint::class.java
        )
    }

    val artistNavigator = remember { entryPoint.getArtistNavigator() }
    val discographyNavigator = remember { entryPoint.getDiscographyNavigator() }
    val searchNavigator = remember { entryPoint.getSearchNavigator() }

    NavHost(
        navController = navController,
        startDestination = SEARCH_ROUTE
    ) {
        // Search Screen - Entry point
        searchNavigator.searchScreen(
            navGraphBuilder = this,
            onArtistClick = { artistId ->
                // Navigate from search to artist details
                artistNavigator.navigateToArtist(
                    navController = navController,
                    artistId = artistId,
                    navOptions = null
                )
            }
        )

        // Artist Screen - Artist details
        artistNavigator.artistScreen(
            navGraphBuilder = this,
            onBackPressed = {
                navController.popBackStack()
            },
            onNavigateToDiscography = { artistId ->
                // Navigate from artist to discography
                discographyNavigator.navigateToDiscography(
                    navController = navController,
                    artistId = artistId,
                    navOptions = null
                )
            }
        )

        // Discography Screen - Artist releases
        discographyNavigator.discographyScreen(
            navGraphBuilder = this,
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}