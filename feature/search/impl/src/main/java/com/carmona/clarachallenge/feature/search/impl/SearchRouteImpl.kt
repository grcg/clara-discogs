/**
 * Search Screen Implementation
 *
 * This file contains the actual implementation of the search screen that
 * gets injected into [SearchScreenProvider] at app startup. It follows the
 * MVVM pattern with a ViewModel managing UI state and business logic.
 */
package com.carmona.clarachallenge.feature.search.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carmona.clarachallenge.feature.search.impl.ui.SearchScreenContent
import com.carmona.clarachallenge.feature.search.impl.ui.SearchViewModel

/**
 * The actual implementation of the search screen composable.
 *
 * This function is created in [SearchModule] and assigned to
 * [SearchScreenProvider.SearchRoute]. It renders the search UI and handles
 * user interactions through the ViewModel.
 *
 * @param onArtistClick Callback when an artist is selected, passed up to the
 *                      navigation graph for screen navigation
 *
 * This function is marked @Composable and uses:
 * - [hiltViewModel] to obtain the Hilt-injected ViewModel
 * - [collectAsStateWithLifecycle] for lifecycle-aware state collection
 * - [SearchScreenContent] for the actual UI implementation
 */
@Composable
fun SearchRouteImpl(
    onArtistClick: (String) -> Unit
) {
    // Get the ViewModel from Hilt
    val viewModel: SearchViewModel = hiltViewModel()

    // Collect state with lifecycle awareness (stops collecting when screen is off)
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Delegate to the content composable that handles the actual UI
    SearchScreenContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::onSearch,
        onLoadMore = viewModel::loadMoreResults,
        onArtistClick = onArtistClick,
        onRetry = viewModel::onRetry,
        onErrorDismissed = viewModel::onErrorDismissed
    )
}