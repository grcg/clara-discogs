package com.carmona.clarachallenge.feature.artist.impl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun ArtistScreen(
    artistId: String,
    onBackPressed: () -> Unit,
    onNavigateToDiscography: (String) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Timber.d("ArtistScreen composable with artistId: $artistId, state.isLoading: ${state.isLoading}, artist: ${state.artist}")

    // Show error messages as they appear in the state
    LaunchedEffect(state.errorMessages) {
        state.errorMessages.forEach { (errorId, errorMessage) ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.handleEvent(ArtistEvent.OnErrorShown(errorId))
            }
        }
    }

    ArtistContent(
        state = state,
        artistId = artistId,
        onBackPressed = onBackPressed,
        onNavigateToDiscography = onNavigateToDiscography,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ArtistContent(
    state: ArtistState,
    artistId: String,
    onBackPressed: () -> Unit,
    onNavigateToDiscography: (String) -> Unit,
    onEvent: (ArtistEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Back button (top left)
        Button(
            onClick = {
                onBackPressed()
                onEvent(ArtistEvent.NavigateBack)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("← Back")
        }

        // Main content
        when {
            state.isLoading && state.artist == null -> {
                // Initial loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.artist != null -> {
                // Show artist data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    item {
                        // Artist name
                        Text(
                            text = state.artist!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Artist bio
                        Text(
                            text = state.artist!!.bio ?: "No biography available",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Genres
                        if (!state.artist!!.genres.isNullOrEmpty()) {
                            Text(
                                text = "Genres: ${state.artist!!.genres!!.joinToString()}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Year formed
                        state.artist!!.yearFormed?.let { year ->
                            Text(
                                text = "Year formed: $year",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Action buttons
                        Button(
                            onClick = {
                                onNavigateToDiscography(artistId)
                                onEvent(ArtistEvent.NavigateToDiscography(artistId))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("View Full Discography")
                        }

                        Button(
                            onClick = {
                                onEvent(ArtistEvent.RefreshArtist)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("Refresh")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Releases section
                        Text(
                            text = "Releases (${state.releases.size} of ${state.totalReleases})",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // Releases list
                    items(state.releases) { release ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = release.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${release.type} - ${release.year ?: "Unknown year"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                release.label?.let {
                                    Text(
                                        text = "Label: $it",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Loading more indicator
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // Load more trigger
                    if (state.hasMorePages && !state.isLoadingMore && state.releases.isNotEmpty()) {
                        item {
                            LaunchedEffect(Unit) {
                                onEvent(ArtistEvent.LoadMoreReleases(state.currentPage + 1))
                            }
                        }
                    }
                }
            }

            else -> {
                // No data and not loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No artist data available",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onEvent(ArtistEvent.LoadArtist(artistId))
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}