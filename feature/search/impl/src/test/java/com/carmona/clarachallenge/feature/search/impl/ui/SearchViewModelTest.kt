package com.carmona.clarachallenge.feature.search.impl.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carmona.clarachallenge.core.domain.usecase.SearchArtistsUseCase
import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.PaginatedResult
import com.carmona.clarachallenge.core.model.Result
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var searchArtistsUseCase: SearchArtistsUseCase

    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun artist(id: String, name: String) = Artist(
        id = id,
        name = name,
        thumbnailUrl = null,
        genres = emptyList()
    )

    private fun page(artists: List<Artist>, page: Int, hasMore: Boolean) = PaginatedResult(
        data = artists,
        page = page,
        hasMorePages = hasMore,
        totalPages = if (hasMore) page + 1 else page,
        totalItems = artists.size
    )

    @Test
    fun `empty query clears results`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        viewModel.onQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assert(state.query.isEmpty())
        assert(state.results.isEmpty())
        assert(!state.isLoading)
        assert(!state.hasMoreResults)
        verify { searchArtistsUseCase wasNot called }
    }

    @Test
    fun `debounced query triggers search`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        val artists = listOf(artist("1", "Korn"))
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(page(artists, 1, false))

        viewModel.onQueryChanged(query)
        verify { searchArtistsUseCase wasNot called }

        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { searchArtistsUseCase(query, 1) }

        val state = viewModel.state.value
        assert(state.results.isNotEmpty())
        assertEquals("Korn", state.results.first().name)
        assert(!state.isLoading)
    }

    @Test
    fun `search executes immediately`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "metallica"
        val artists = listOf(artist("1", "Metallica"))
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(page(artists, 1, true))

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(artists, state.results)
        assert(state.hasMoreResults)
        assert(state.recentSearches.contains(query))
    }

    @Test
    fun `load more appends results`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        val page1Artists = listOf(artist("1", "Korn1"), artist("2", "Korn2"))
        val page2Artists = listOf(artist("3", "Korn3"), artist("4", "Korn4"))

        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(page(page1Artists, 1, true))
        coEvery { searchArtistsUseCase(query, 2) } returns Result.Success(page(page2Artists, 2, false))

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assert(state.results.containsAll(page1Artists))
        assert(state.results.containsAll(page2Artists))
        assertEquals(2, state.currentPage)
        assert(!state.hasMoreResults)

        coVerify(exactly = 1) { searchArtistsUseCase(query, 1) }
        coVerify(exactly = 1) { searchArtistsUseCase(query, 2) }
    }

    @Test
    fun `load more prevents duplicate requests while loading`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        val page1Artists = listOf(artist("1", "Korn"))
        val deferred = CompletableDeferred<Result<PaginatedResult<Artist>>>()

        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(page(page1Artists, 1, true))
        coEvery { searchArtistsUseCase(query, 2) } coAnswers { deferred.await() }

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(!viewModel.state.value.isLoading)
        assertEquals(page1Artists, viewModel.state.value.results)
        assert(viewModel.state.value.hasMoreResults)

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.state.value.isLoadingMore)

        repeat(3) {
            viewModel.loadMoreResults()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        coVerify(exactly = 1) { searchArtistsUseCase(query, 2) }

        deferred.complete(Result.Success(page(emptyList(), 2, false)))
        testDispatcher.scheduler.advanceUntilIdle()

        assert(!viewModel.state.value.isLoadingMore)
    }

    @Test
    fun `load more ignored when no pages left`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(
            page(listOf(artist("1", "Korn")), 1, false)
        )

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(!viewModel.state.value.hasMoreResults)

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { searchArtistsUseCase(query, 1) }
        coVerify(exactly = 0) { searchArtistsUseCase(query, 2) }
    }

    @Test
    fun `search error updates state`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Error("Network error occurred")

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assert(state.results.isEmpty())
        assert(state.errorMessages.isNotEmpty())
        assertEquals("Network error occurred", state.errorMessages.values.first())
    }

    @Test
    fun `error dismissed clears error`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Error("Network error occurred")

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        val errorId = viewModel.state.value.errorMessages.keys.first()
        viewModel.onErrorDismissed(errorId)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.state.value.errorMessages.isEmpty())
    }

    @Test
    fun `retry triggers new search`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        val artists = listOf(artist("1", "Korn"))

        coEvery { searchArtistsUseCase(query, 1) } returnsMany listOf(
            Result.Error("Network error"),
            Result.Success(page(artists, 1, false))
        )

        viewModel.onSearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.state.value
        assert(state.errorMessages.isNotEmpty())
        assert(state.results.isEmpty())

        viewModel.onRetry()
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.state.value
        assertEquals(artists, state.results)
        assert(state.errorMessages.isEmpty())

        coVerify(exactly = 2) { searchArtistsUseCase(query, 1) }
    }

    @Test
    fun `recent searches limited to 10`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        coEvery { searchArtistsUseCase(any(), 1) } returns Result.Success(page(emptyList(), 1, false))

        (1..15).forEach { i ->
            viewModel.onSearch("search$i")
            testDispatcher.scheduler.advanceUntilIdle()
        }

        val recent = viewModel.state.value.recentSearches
        assertEquals(10, recent.size)
        assertEquals("search15", recent.first())
        assertEquals("search6", recent.last())
    }

    @Test
    fun `duplicate searches appear only once`() = runTest(testDispatcher) {
        viewModel = SearchViewModel(searchArtistsUseCase)
        val query = "korn"
        coEvery { searchArtistsUseCase(query, 1) } returns Result.Success(page(emptyList(), 1, false))

        repeat(3) {
            viewModel.onSearch(query)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        val recent = viewModel.state.value.recentSearches
        assertEquals(1, recent.size)
        assertEquals(query, recent.first())
    }
}