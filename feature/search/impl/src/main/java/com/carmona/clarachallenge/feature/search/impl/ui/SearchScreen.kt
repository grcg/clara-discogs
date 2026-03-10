/**
 * Search Screen Implementation
 *
 * This file contains the UI implementation for the artist search feature.
 * It displays a search bar and a list of search results with pagination.
 *
 * The screen follows Material Design 3 guidelines and includes:
 * - A search bar with debounced input
 * - Recent search suggestions
 * - Paginated results list
 * - Loading and error states
 * - Pull to refresh (optional)
 */
package com.carmona.clarachallenge.feature.search.impl.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.carmona.clarachallenge.core.model.Artist

/**
 * Main entry point for the search screen composable.
 *
 * This function is called from the SearchNavigator and provides
 * the fully functional search screen with ViewModel integration.
 *
 * @param onArtistClick Callback when an artist is selected for navigation
 */
@Composable
fun SearchScreen(
    onArtistClick: (String) -> Unit
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

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

/**
 * Main content for the search screen.
 *
 * This composable contains the actual UI implementation, separated from
 * the ViewModel for better testability and reusability.
 *
 * @param state Current UI state from ViewModel
 * @param onQueryChanged Callback when search query changes
 * @param onSearch Callback when search is submitted
 * @param onLoadMore Callback to load more results (pagination)
 * @param onArtistClick Callback when an artist is selected
 * @param onRetry Callback to retry failed search
 * @param onErrorDismissed Callback to dismiss error messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    state: SearchState,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onLoadMore: () -> Unit,
    onArtistClick: (String) -> Unit,
    onRetry: () -> Unit,
    onErrorDismissed: (Long) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var searchBarActive by rememberSaveable { mutableStateOf(false) }

    // Handle pagination trigger when reaching the end of the list
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                    lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3 &&
                    state.hasMoreResults &&  // Now properly references the state property
                    !state.isLoadingMore &&   // Now properly references the state property
                    state.results.isNotEmpty() // Now properly references the state property
        }
    }

    // Trigger load more when reaching the end
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    // Show errors in snackbar
    LaunchedEffect(state.errorMessages) {
        state.errorMessages.forEach { (errorId, message) ->
            snackbarHostState.showSnackbar(message)
            onErrorDismissed(errorId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Search") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = state.query,
                onQueryChange = onQueryChanged,
                onSearch = {
                    focusManager.clearFocus()
                    onSearch(state.query)
                    searchBarActive = false
                },
                active = searchBarActive,
                onActiveChange = { searchBarActive = it },
                placeholder = { Text("Search for artists...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear query"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search suggestions (recent searches)
                if (state.query.isNotEmpty() && state.recentSearches.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        state.recentSearches.take(5).forEach { recent ->
                            Text(
                                text = recent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onQueryChanged(recent)
                                        onSearch(recent)
                                        searchBarActive = false
                                        focusManager.clearFocus()
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading && state.results.isEmpty() -> {
                        // Initial loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.errorMessages.isNotEmpty() && state.results.isEmpty() -> {
                        // Error with no results
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load artists",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = onRetry) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.results.isEmpty() -> {
                        // No results
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.query.isEmpty())
                                    "Start searching for artists"
                                else "No artists found"
                            )
                        }
                    }

                    else -> {
                        // Results list
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.results,
                                key = { it.id }
                            ) { artist ->
                                ArtistSearchResultItem(
                                    artist = artist,
                                    onArtistClick = onArtistClick
                                )
                            }

                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual artist search result item.
 *
 * This composable displays a single artist result in the search results list.
 * It shows the artist's image, name, and genres (if available).
 *
 * @param artist The artist data to display
 * @param onArtistClick Callback when the item is clicked, passing the artist ID
 */
@Composable
fun ArtistSearchResultItem(
    artist: Artist,
    onArtistClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArtistClick(artist.id) },  // artist.id is already a String
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist Image
            AsyncImage(
                model = artist.thumbnailUrl ?: "https://via.placeholder.com/150",
                contentDescription = artist.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Artist Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                if (!artist.genres.isNullOrEmpty()) {
                    Text(
                        text = (artist.genres as Iterable<Any?>).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Optionally show year formed if available
                if (artist.yearFormed != null) {
                    Text(
                        text = "Formed: ${artist.yearFormed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}