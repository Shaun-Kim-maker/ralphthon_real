package com.ralphthon.app.ui.search

import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.usecase.SearchCardsUseCase
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class SearchStateTest {

    @MockK
    private lateinit var searchCardsUseCase: SearchCardsUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeResult(
        cardId: Long = 1L,
        customerId: Long = 1L,
        companyName: String = "삼성전자",
        title: String = "미팅 요약",
        snippet: String = "가격 협의 내용",
        score: Float = 0.9f
    ) = SearchResult(cardId, customerId, companyName, title, snippet, score)

    private fun createViewModel() = SearchViewModel(searchCardsUseCase)

    // === Initial state (1-3) ===

    @Test
    fun should_beIdle_when_initialState() = runTest {
        val vm = createViewModel()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_haveEmptyRecentSearches_when_initialState() = runTest {
        val vm = createViewModel()
        assertTrue(vm.recentSearches.value.isEmpty())
    }

    @Test
    fun should_remainIdle_when_emptyQuerySearched() = runTest {
        val vm = createViewModel()
        vm.search("   ")
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    // === Loading state transitions (4-7) ===

    @Test
    fun should_transitionToLoading_when_validQuerySearched() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        // After idle, state should be Data (went through Loading)
        assertTrue(vm.uiState.value is SearchUiState.Data)
    }

    @Test
    fun should_transitionIdleToLoading_when_searchStarts() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Data)
    }

    @Test
    fun should_transitionLoadingToData_when_resultsFound() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Data)
    }

    @Test
    fun should_transitionLoadingToEmpty_when_noResultsFound() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        vm.search("존재하지않는쿼리")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Empty)
    }

    // === Data state (8-11) ===

    @Test
    fun should_containResultsInData_when_searchSucceeds() = runTest {
        val results = listOf(makeResult(cardId = 1L), makeResult(cardId = 2L))
        coEvery { searchCardsUseCase(any()) } returns Result.success(results)
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Data
        assertEquals(2, state.results.size)
    }

    @Test
    fun should_containQueryInData_when_searchSucceeds() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성전자")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Data
        assertEquals("삼성전자", state.query)
    }

    @Test
    fun should_containQueryInEmpty_when_noResultsFound() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        vm.search("없는쿼리")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Empty
        assertEquals("없는쿼리", state.query)
    }

    @Test
    fun should_trimQueryBeforeSearch_when_queryHasWhitespace() = runTest {
        coEvery { searchCardsUseCase("삼성") } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("  삼성  ")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Data
        assertEquals("삼성", state.query)
    }

    // === Error state transitions (12-15) ===

    @Test
    fun should_transitionToError_when_networkExceptionThrown() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
    }

    @Test
    fun should_containNetworkErrorMessage_when_networkExceptionThrown() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_containTimeoutErrorMessage_when_timeoutExceptionThrown() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    @Test
    fun should_containServerErrorMessage_when_serverExceptionThrown() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Error
        assertEquals("서버 오류가 발생했습니다", state.message)
    }

    // === clearSearch (16-18) ===

    @Test
    fun should_transitionToIdle_when_clearSearchCalled() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        vm.clearSearch()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_remainIdle_when_clearSearchCalledOnIdle() = runTest {
        val vm = createViewModel()
        vm.clearSearch()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_transitionErrorToIdle_when_clearSearchCalled() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
        vm.clearSearch()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    // === Recent searches (19-24) ===

    @Test
    fun should_addToRecentSearches_when_searchSucceeds() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.contains("삼성"))
    }

    @Test
    fun should_addToRecentSearches_when_searchReturnsEmpty() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        vm.search("없는것")
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.contains("없는것"))
    }

    @Test
    fun should_notAddToRecentSearches_when_searchFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.isEmpty())
    }

    @Test
    fun should_deduplicateRecentSearches_when_sameQuerySearchedTwice() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        vm.search("삼성")
        advanceUntilIdle()
        assertEquals(1, vm.recentSearches.value.count { it == "삼성" })
    }

    @Test
    fun should_putMostRecentFirst_when_duplicateQuerySearched() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("LG")
        advanceUntilIdle()
        vm.search("삼성")
        advanceUntilIdle()
        vm.search("LG")
        advanceUntilIdle()
        assertEquals("LG", vm.recentSearches.value.first())
    }

    @Test
    fun should_removeFromRecentSearches_when_removeRecentSearchCalled() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.search("삼성")
        advanceUntilIdle()
        vm.removeRecentSearch("삼성")
        assertTrue(vm.recentSearches.value.none { it == "삼성" })
    }

    // === searchFiltered (25-30) ===

    @Test
    fun should_transitionToData_when_filteredSearchSucceeds() = runTest {
        coEvery { searchCardsUseCase.searchFiltered(any(), any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.searchFiltered("삼성", "CUSTOMER_MEETING")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Data)
    }

    @Test
    fun should_transitionToEmpty_when_filteredSearchReturnsEmpty() = runTest {
        coEvery { searchCardsUseCase.searchFiltered(any(), any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        vm.searchFiltered("삼성", "INTERNAL_MEETING")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Empty)
    }

    @Test
    fun should_transitionToError_when_filteredSearchFails() = runTest {
        coEvery { searchCardsUseCase.searchFiltered(any(), any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        vm.searchFiltered("삼성", null)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
    }

    @Test
    fun should_remainIdle_when_filteredSearchWithEmptyQuery() = runTest {
        val vm = createViewModel()
        vm.searchFiltered("  ", "CUSTOMER_MEETING")
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_addToRecentSearches_when_filteredSearchSucceeds() = runTest {
        coEvery { searchCardsUseCase.searchFiltered(any(), any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        vm.searchFiltered("LG전자", null)
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.contains("LG전자"))
    }

    @Test
    fun should_capRecentSearchesAtTen_when_moreThanTenQueriesSearched() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(listOf(makeResult()))
        val vm = createViewModel()
        for (i in 1..12) {
            vm.search("query$i")
            advanceUntilIdle()
        }
        assertEquals(10, vm.recentSearches.value.size)
    }
}
