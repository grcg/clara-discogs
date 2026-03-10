/**
 * Wrapper class for the search screen composable.
 *
 * This class solves the Hilt type inference issues with complex composable function types.
 * It provides a simple wrapper around the composable function that Hilt can easily inject.
 */
package com.carmona.clarachallenge.feature.search.api

import androidx.compose.runtime.Composable

/**
 * Simple wrapper interface for the search screen composable.
 * Hilt can easily inject this interface without complex type inference issues.
 */
interface SearchScreen {
    @Composable
    fun Content(onArtistClick: (String) -> Unit)
}