package com.carmona.clarachallenge.feature.discography.impl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carmona.clarachallenge.core.model.Release
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Discography screen composable
 *
 * Displays a paginated list of releases for a specific artist with filtering capabilities.
 *
 * @param artistId The ID of the artist whose discography to display (as String to match domain model)
 * @param onBackPressed Callback when user navigates back
 * @param viewModel Hilt-injected ViewModel for discography feature
 */
@Composable
fun DiscographyScreen(
    artistId: String,
    onBackPressed: () -> Unit,
    viewModel: DiscographyViewModel = hiltViewModel()
) {
    // Collect state with lifecycle awareness
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initialize with artistId if needed
    LaunchedEffect(artistId) {
        // Send event to load releases for this artist
        viewModel.handleEvent(DiscographyEvent.LoadReleases(artistId, page = 1))
    }

    // Show error messages when they appear in state
    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
                // Clear error by sending event
                viewModel.handleEvent(DiscographyEvent.ErrorShown)
            }
        }
    }

    DiscographyContent(
        state = state,
        onBackPressed = onBackPressed,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState,
        listState = listState
    )
}

/**
 * Content composable for discography screen
 *
 * @param state Current UI state from ViewModel
 * @param onBackPressed Callback for back navigation
 * @param onEvent Callback to send events to ViewModel
 * @param snackbarHostState State for Snackbar error display
 * @param listState State for LazyColumn scroll position
 */
@Composable
fun DiscographyContent(
    state: DiscographyState,
    onBackPressed: () -> Unit,
    onEvent: (DiscographyEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Custom TopBar with back button and title
            DiscographyTopBar(
                artistName = state.artistName,
                onBackPressed = onBackPressed
            )

            // Filter section (bonus feature from requirements)
            if (state.showFilters) {
                FilterSection(
                    onApplyFilters = { filters ->
                        onEvent(DiscographyEvent.ClearFilters)
                    },
                    onToggleFilters = {
                        // Toggle filter visibility - you might need to add this to your state/events
                    }
                )
            }

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    // Loading state
                    state.isLoading && state.releases.isEmpty() -> {
                        LoadingState()
                    }

                    // Error state
                    state.error != null && state.releases.isEmpty() -> {
                        ErrorState(
                            message = state.error,
                            onRetry = {
                                onEvent(DiscographyEvent.LoadReleases(state.artistId, page = 1))
                            }
                        )
                    }

                    // Empty state
                    !state.isLoading && state.releases.isEmpty() -> {
                        EmptyState()
                    }

                    // Success state with releases
                    else -> {
                        ReleasesList(
                            releases = state.releases,
                            isLoadingMore = state.isLoadingMore,
                            hasMorePages = state.hasMorePages,
                            onLoadMore = {
                                onEvent(DiscographyEvent.LoadMoreReleases(state.currentPage + 1))
                            },
                            onReleaseClick = { releaseId ->
                                onEvent(DiscographyEvent.OnReleaseClick(releaseId))
                            },
                            listState = listState
                        )
                    }
                }
            }
        }

        // Snackbar for error messages (overlay at bottom)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/**
 * Top bar with back navigation and artist name
 */
@Composable
fun DiscographyTopBar(
    artistName: String,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = if (artistName.isNotEmpty()) "Discography: $artistName" else "Discography",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Filter section for sorting/filtering releases
 * Implements requirement: "filtering options in the discography, allowing users to filter albums by year, genre, or label"
 */
@Composable
fun FilterSection(
    onApplyFilters: (Map<String, String>) -> Unit,
    onToggleFilters: () -> Unit
) {
    // TODO: Implement actual filter UI
    // This would include:
    // - Year range selector
    // - Genre dropdown/multiselect
    // - Label text search

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder for filter controls
            Text(
                text = "Filter by year, genre, or label (coming soon)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onApplyFilters(emptyMap()) }, // Apply filters
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

/**
 * Loading state - centered progress indicator
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state with retry button
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading releases",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state - no releases found
 */
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No releases found for this artist",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Lazy list of releases with infinite scroll pagination
 *
 * @param releases List of releases to display
 * @param isLoadingMore Whether more items are currently being loaded
 * @param hasMorePages Whether there are more pages to load
 * @param onLoadMore Callback to load more items
 * @param onReleaseClick Callback when a release is clicked
 * @param listState State for scroll position
 */
@Composable
fun ReleasesList(
    releases: List<Release>,
    isLoadingMore: Boolean,
    hasMorePages: Boolean,
    onLoadMore: () -> Unit,
    onReleaseClick: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display all releases
        items(
            items = releases,
            key = { release -> release.id } // Stable unique key for each item
        ) { release ->
            ReleaseItem(
                release = release,
                onReleaseClick = { onReleaseClick(release.id) }
            )
        }

        // Loading more indicator at the bottom
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Trigger to load more when reaching the end
        if (hasMorePages && !isLoadingMore && releases.isNotEmpty()) {
            item {
                LaunchedEffect(Unit) {
                    // Small delay to avoid triggering too quickly
                    delay(100)
                    onLoadMore()
                }
                // Invisible spacer to trigger the effect
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}

/**
 * Individual release item card
 *
 * Displays album information including:
 * - Title
 * - Type (album, single, EP)
 * - Year
 * - Label
 * - Genre
 * - Track count
 */
@Composable
fun ReleaseItem(
    release: Release,
    onReleaseClick: () -> Unit
) {
    Card(
        onClick = onReleaseClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = release.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Release type and year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Format type for display (capitalize first letter)
                val displayType = release.type.replaceFirstChar { it.uppercase() }
                Text(
                    text = displayType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (release.year != null) {
                    Text(
                        text = release.year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Label (if available)
            release.label?.let { label ->
                if (label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Label: $label",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Genre (if available)
            release.genre?.let { genre ->
                if (genre.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genre: $genre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Track count (if available)
            release.trackCount?.let { trackCount ->
                if (trackCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$trackCount tracks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}