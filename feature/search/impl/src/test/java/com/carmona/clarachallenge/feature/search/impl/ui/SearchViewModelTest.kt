package com.carmona.clarachallenge.feature.search.impl.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carmona.clarachallenge.core.domain.usecase.SearchArtistsUseCase
import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.PaginatedResult
import com.carmona.clarachallenge.core.model.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var searchArtistsUseCase: SearchArtistsUseCase

    private lateinit var viewModel: SearchViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(searchArtistsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun artist(id: String, name: String) =
        Artist(id = id, name = name, thumbnailUrl = null, genres = emptyList())

    private fun page(
        artists: List<Artist>,
        page: Int,
        hasMore: Boolean
    ) = PaginatedResult(
        data = artists,
        page = page,
        hasMorePages = hasMore,
        totalPages = if (hasMore) page + 1 else page,
        totalItems = artists.size
    )

    @Test
    fun `empty query clears results`() = runTest(testDispatcher) {
        viewModel.onQueryChanged("")

        val state = viewModel.state.value

        assert(state.query.isEmpty())
        assert(state.results.isEmpty())
        assert(!state.isLoading)
        assert(!state.hasMoreResults)  // FIXED: Use hasMoreResults, not hasMorePages

        verifyNoMoreInteractions(searchArtistsUseCase)
    }

    @Test
    fun `debounced query triggers search`() = runTest(testDispatcher) {
        val query = "korn"
        val artists = listOf(artist("1", "Korn"))

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(artists, 1, false)))

        viewModel.onQueryChanged(query)

        // Verify no immediate call
        verify(searchArtistsUseCase, never()).invoke(any(), any())

        // Advance time by 500ms to trigger debounce
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.runCurrent()

        verify(searchArtistsUseCase).invoke(query, 1)

        val state = viewModel.state.value
        assert(state.results.isNotEmpty())
        assert(state.results.first().name == "Korn")
        assert(!state.isLoading)
    }

    @Test
    fun `search executes immediately`() = runTest(testDispatcher) {
        val query = "metallica"
        val artists = listOf(artist("1", "Metallica"))

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(artists, 1, true)))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assert(state.results == artists)
        assert(state.hasMoreResults)  // FIXED: Use hasMoreResults
        assert(state.recentSearches.contains(query))
    }

    @Test
    fun `load more appends results`() = runTest(testDispatcher) {
        val query = "korn"

        val page1Artists = listOf(
            artist("1", "Korn1"),
            artist("2", "Korn2")
        )

        val page2Artists = listOf(
            artist("3", "Korn3"),
            artist("4", "Korn4")
        )

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(page1Artists, 1, true)))

        whenever(searchArtistsUseCase.invoke(query, 2))
            .thenReturn(Result.Success(page(page2Artists, 2, false)))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        viewModel.loadMoreResults()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assert(state.results.containsAll(page1Artists))
        assert(state.results.containsAll(page2Artists))
        assert(state.currentPage == 2)
        assert(!state.hasMoreResults)  // FIXED: Use hasMoreResults

        verify(searchArtistsUseCase).invoke(query, 1)
        verify(searchArtistsUseCase).invoke(query, 2)
    }

    @Test
    fun `load more prevents duplicate requests while loading`() = runTest(testDispatcher) {
        val query = "korn"
        val page1Artists = listOf(artist("1", "Korn"))

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(page1Artists, 1, true)))

        // FIXED: Use runBlocking inside thenAnswer to handle suspend function
        val deferred = CompletableDeferred<Result<PaginatedResult<Artist>>>()
        whenever(searchArtistsUseCase.invoke(query, 2))
            .thenAnswer {
                runBlocking {
                    deferred.await()
                }
            }

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        // Verify first page loaded
        assert(!viewModel.state.value.isLoading)
        assert(viewModel.state.value.results == page1Artists)
        assert(viewModel.state.value.hasMoreResults)  // FIXED: Use hasMoreResults

        // First load more request
        viewModel.loadMoreResults()
        testDispatcher.scheduler.runCurrent()

        // Verify loading more state is true
        assert(viewModel.state.value.isLoadingMore)

        // Try to load more multiple times while first is in progress
        repeat(3) {
            viewModel.loadMoreResults()
            testDispatcher.scheduler.runCurrent()
        }

        // Verify only one call was made
        verify(searchArtistsUseCase, times(1)).invoke(query, 2)

        // Complete the deferred call with success
        deferred.complete(Result.Success(page(emptyList(), 2, false)))
        testDispatcher.scheduler.runCurrent()

        // Verify loading more is finished
        assert(!viewModel.state.value.isLoadingMore)
    }

    @Test
    fun `load more ignored when no pages left`() = runTest(testDispatcher) {
        val query = "korn"

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(listOf(artist("1", "Korn")), 1, false)))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assert(!state.hasMoreResults)  // FIXED: Use hasMoreResults

        viewModel.loadMoreResults()
        testDispatcher.scheduler.runCurrent()

        verify(searchArtistsUseCase, times(1)).invoke(query, 1)
        verifyNoMoreInteractions(searchArtistsUseCase)
    }

    @Test
    fun `search error updates state`() = runTest(testDispatcher) {
        val query = "korn"

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Error("Network error occurred"))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value
        assert(state.results.isEmpty())
        assert(state.errorMessages.isNotEmpty())
        assert(state.errorMessages.values.first() == "Network error occurred")
    }

    @Test
    fun `error dismissed clears error`() = runTest(testDispatcher) {
        val query = "korn"

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Error("Network error occurred"))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        val errorId = viewModel.state.value.errorMessages.keys.first()
        viewModel.onErrorDismissed(errorId)
        testDispatcher.scheduler.runCurrent()

        assert(viewModel.state.value.errorMessages.isEmpty())
    }

    @Test
    fun `retry triggers new search`() = runTest(testDispatcher) {
        val query = "korn"
        val artists = listOf(artist("1", "Korn"))

        // First call fails, second call succeeds
        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Error("Network error"))
            .thenReturn(Result.Success(page(artists, 1, false)))

        viewModel.onSearch(query)
        testDispatcher.scheduler.runCurrent()

        // Verify error state
        var state = viewModel.state.value
        assert(state.errorMessages.isNotEmpty())
        assert(state.results.isEmpty())

        viewModel.onRetry()
        testDispatcher.scheduler.runCurrent()

        // Verify success state after retry
        state = viewModel.state.value
        assert(state.results == artists)
        assert(state.errorMessages.isEmpty())

        verify(searchArtistsUseCase, times(2)).invoke(query, 1)
    }

    @Test
    fun `recent searches limited to 10`() = runTest(testDispatcher) {
        whenever(searchArtistsUseCase.invoke(any(), eq(1)))
            .thenReturn(Result.Success(page(emptyList(), 1, false)))

        (1..15).forEach { i ->
            viewModel.onSearch("search$i")
            testDispatcher.scheduler.runCurrent()
        }

        val recent = viewModel.state.value.recentSearches
        assert(recent.size == 10)
        assert(recent.first() == "search15")
        assert(recent.last() == "search6")
    }

    @Test
    fun `duplicate searches appear only once`() = runTest(testDispatcher) {
        val query = "korn"

        whenever(searchArtistsUseCase.invoke(query, 1))
            .thenReturn(Result.Success(page(emptyList(), 1, false)))

        repeat(3) {
            viewModel.onSearch(query)
            testDispatcher.scheduler.runCurrent()
        }

        val recent = viewModel.state.value.recentSearches
        assert(recent.size == 1)
        assert(recent.first() == query)
    }
}