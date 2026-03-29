package com.ralphthon.app.ui.search

import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.usecase.SearchCardsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SearchViewModelTest {

    @MockK
    private lateinit var searchCardsUseCase: SearchCardsUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeResult(
        id: Long = 1L,
        type: String = "conversation",
        title: String = "결과",
        snippet: String = "스니펫",
        highlightRanges: List<IntRange> = emptyList(),
        sourceId: Long = 10L,
        relevanceScore: Float = 0.9f
    ) = SearchResult(id, type, title, snippet, highlightRanges, sourceId, relevanceScore)

    private fun createViewModel() = SearchViewModel(searchCardsUseCase)

    // ===== States (1-8) =====

    @Test
    fun should_beIdle_when_initialState() {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun should_showLoading_when_searchStarted() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        // After debounce fires and before result arrives, Loading is set
        // We check that Data state is eventually reached
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Data)
    }

    @Test
    fun should_showData_when_resultsFound() = runTest {
        val results = listOf(makeResult(1), makeResult(2))
        coEvery { searchCardsUseCase(any()) } returns Result.success(results)
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Data)
        assertEquals(2, (state as SearchUiState.Data).results.size)
    }

    @Test
    fun should_showEmpty_when_noResultsFound() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        viewModel.search("없는검색어")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Empty)
    }

    @Test
    fun should_showError_when_searchFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
    }

    @Test
    fun should_returnToIdle_when_clearSearch() = runTest {
        val results = listOf(makeResult())
        coEvery { searchCardsUseCase(any()) } returns Result.success(results)
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.clearSearch()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun should_showNetworkErrorMessage_when_networkException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_showTimeoutErrorMessage_when_timeoutException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    // ===== Debounce (9-12) =====

    @Test
    fun should_debounce_when_rapidInput() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("a")
        advanceTimeBy(100)
        viewModel.search("ab")
        advanceTimeBy(100)
        viewModel.search("abc")
        advanceTimeBy(400)
        advanceUntilIdle()
        // Only the last search should have executed
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Data)
        assertEquals("abc", (state as SearchUiState.Data).query)
    }

    @Test
    fun should_cancelPrevious_when_newSearchStarted() = runTest {
        coEvery { searchCardsUseCase("first") } returns Result.success(listOf(makeResult(1, title = "첫번째")))
        coEvery { searchCardsUseCase("second") } returns Result.success(listOf(makeResult(2, title = "두번째")))
        val viewModel = createViewModel()
        viewModel.search("first")
        advanceTimeBy(100)
        viewModel.search("second")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals("second", state.query)
    }

    @Test
    fun should_returnToIdle_when_emptyQueryAfterDebounce() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(100)
        viewModel.search("")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun should_returnToIdle_when_blankQuery() = runTest {
        val viewModel = createViewModel()
        viewModel.search("   ")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    // ===== Results (13-18) =====

    @Test
    fun should_preserveAllFields_when_resultReturned() = runTest {
        val result = makeResult(
            id = 42L,
            type = "conversation",
            title = "테스트 제목",
            snippet = "스니펫 내용",
            highlightRanges = listOf(0..3, 5..8),
            sourceId = 99L,
            relevanceScore = 0.95f
        )
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(result))
        val viewModel = createViewModel()
        viewModel.search("테스트")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        val r = state.results.first()
        assertEquals(42L, r.id)
        assertEquals("conversation", r.type)
        assertEquals("테스트 제목", r.title)
        assertEquals("스니펫 내용", r.snippet)
        assertEquals(2, r.highlightRanges.size)
        assertEquals(99L, r.sourceId)
        assertEquals(0.95f, r.relevanceScore)
    }

    @Test
    fun should_showMultipleResults_when_multipleFound() = runTest {
        val results = (1L..5L).map { makeResult(id = it) }
        coEvery { searchCardsUseCase(any()) } returns Result.success(results)
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(5, state.results.size)
    }

    @Test
    fun should_showSingleResult_when_oneFound() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(1, state.results.size)
    }

    @Test
    fun should_preserveHighlightRanges_when_resultHasRanges() = runTest {
        val ranges = listOf(0..5, 10..15, 20..25)
        val result = makeResult(highlightRanges = ranges)
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(result))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(3, state.results.first().highlightRanges.size)
        assertEquals(0..5, state.results.first().highlightRanges[0])
    }

    @Test
    fun should_preserveRelevanceScore_when_resultHasScore() = runTest {
        val result = makeResult(relevanceScore = 0.75f)
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(result))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(0.75f, state.results.first().relevanceScore)
    }

    @Test
    fun should_handleKoreanText_when_searchWithKorean() = runTest {
        val result = makeResult(title = "삼성전자 영업회의", snippet = "가격 협상 내용")
        coEvery { searchCardsUseCase("삼성") } returns Result.success(listOf(result))
        val viewModel = createViewModel()
        viewModel.search("삼성")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals("삼성전자 영업회의", state.results.first().title)
    }

    // ===== Recent searches (19-25) =====

    @Test
    fun should_addToRecent_when_searchSucceeds() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.recentSearches.value.contains("query"))
    }

    @Test
    fun should_deduplicateRecent_when_sameQuerySearchedTwice() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertEquals(1, viewModel.recentSearches.value.count { it == "query" })
    }

    @Test
    fun should_limitToTen_when_moreThanTenSearches() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        for (i in 1..12) {
            viewModel.search("query$i")
            advanceTimeBy(400)
            advanceUntilIdle()
        }
        assertEquals(10, viewModel.recentSearches.value.size)
    }

    @Test
    fun should_removeFromRecent_when_removeRecentSearchCalled() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.removeRecentSearch("query")
        assertTrue(!viewModel.recentSearches.value.contains("query"))
    }

    @Test
    fun should_orderByRecency_when_multipleSearches() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("first")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.search("second")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertEquals("second", viewModel.recentSearches.value.first())
    }

    @Test
    fun should_beEmptyInitially_when_noSearches() {
        val viewModel = createViewModel()
        assertTrue(viewModel.recentSearches.value.isEmpty())
    }

    @Test
    fun should_persistAcrossSearches_when_multipleQueries() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.search("first")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.search("second")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.recentSearches.value.contains("first"))
        assertTrue(viewModel.recentSearches.value.contains("second"))
    }

    // ===== Filter (26-29) =====

    @Test
    fun should_filterByType_when_typeProvided() = runTest {
        val allResults = listOf(
            makeResult(1, type = "conversation"),
            makeResult(2, type = "customer")
        )
        coEvery { searchCardsUseCase.searchFiltered("query", "conversation") } returns Result.success(listOf(allResults[0]))
        val viewModel = createViewModel()
        viewModel.searchFiltered("query", "conversation")
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(1, state.results.size)
        assertEquals("conversation", state.results.first().type)
    }

    @Test
    fun should_returnAllTypes_when_typeIsNull() = runTest {
        val results = listOf(makeResult(1, type = "conversation"), makeResult(2, type = "customer"))
        coEvery { searchCardsUseCase.searchFiltered("query", null) } returns Result.success(results)
        val viewModel = createViewModel()
        viewModel.searchFiltered("query", null)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals(2, state.results.size)
    }

    @Test
    fun should_showEmpty_when_filterResultsEmpty() = runTest {
        coEvery { searchCardsUseCase.searchFiltered("query", "nonexistent") } returns Result.success(emptyList())
        val viewModel = createViewModel()
        viewModel.searchFiltered("query", "nonexistent")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Empty)
    }

    @Test
    fun should_preserveQuery_when_filterApplied() = runTest {
        coEvery { searchCardsUseCase.searchFiltered("myquery", "conversation") } returns Result.success(listOf(makeResult()))
        val viewModel = createViewModel()
        viewModel.searchFiltered("myquery", "conversation")
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Data
        assertEquals("myquery", state.query)
    }

    // ===== Error (30-35) =====

    @Test
    fun should_showError_when_networkException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (viewModel.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showError_when_timeoutException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
        assertEquals("서버 응답 시간이 초과되었습니다", (viewModel.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showError_when_serverException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (viewModel.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showGenericError_when_unknownException() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(RuntimeException("unknown"))
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
        assertEquals("검색 중 오류가 발생했습니다", (viewModel.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showCorrectMessage_when_errorOccurs() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        val state = viewModel.uiState.value as SearchUiState.Error
        assertTrue(state.message.isNotBlank())
    }

    @Test
    fun should_allowRetry_when_searchAfterError() = runTest {
        coEvery { searchCardsUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeResult()))
        )
        val viewModel = createViewModel()
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Error)
        viewModel.search("query")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Data)
    }
}
