package com.ralphthon.app.ui.card

import androidx.lifecycle.SavedStateHandle
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.usecase.GetCardsByCustomerUseCase
import io.mockk.MockKAnnotations
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CardNewsListStateTest {

    @MockK
    private lateinit var getCardsByCustomerUseCase: GetCardsByCustomerUseCase

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

    private fun makeCard(
        id: Long = 1L,
        customerId: Long = 1L,
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING
    ) = ContextCard.withDefaults(
        id = id,
        conversationId = id,
        customerId = customerId,
        title = "카드 $id",
        date = "2026-03-01",
        conversationType = conversationType,
        sentiment = Sentiment.POSITIVE,
        sentimentScore = 0.8f
    )

    private fun createViewModel(customerId: Long = 1L): CardNewsListViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("customerId" to customerId))
        return CardNewsListViewModel(getCardsByCustomerUseCase, savedStateHandle)
    }

    // ===== State transitions (1-12) =====

    @Test
    fun should_startWithLoading_when_created() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionToData_when_cardsLoaded() = runTest {
        val cards = listOf(makeCard(1), makeCard(2))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
        assertEquals(2, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_transitionToEmpty_when_noCards() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_transitionToError_when_loadFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
    }

    @Test
    fun should_transitionLoadingToData_when_success() = runTest {
        val cards = listOf(makeCard(1))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        // Initially Loading
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        // Then Data
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_transitionLoadingToError_when_failure() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
    }

    @Test
    fun should_transitionErrorToLoading_when_reloaded() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
        viewModel.loadCards()
        // After calling loadCards(), Loading is set synchronously
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionErrorToData_when_reloadSucceeds() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_transitionDataToData_when_refreshed() = runTest {
        val initial = listOf(makeCard(1))
        val refreshed = listOf(makeCard(2), makeCard(3))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(initial),
            Result.success(refreshed)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
        viewModel.loadCards()
        advanceUntilIdle()
        assertEquals(2, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_transitionEmptyToLoading_when_reloaded() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
        viewModel.loadCards()
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionEmptyToData_when_reloadSucceeds() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_keepErrorState_when_reloadFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
    }

    // ===== Pagination state (13-22) =====

    @Test
    fun should_setHasMore_when_fullPageReturned() = runTest {
        val cards = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue((viewModel.uiState.value as CardNewsListUiState.Data).hasMore)
    }

    @Test
    fun should_clearHasMore_when_partialPage() = runTest {
        val cards = (1L..7L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardNewsListUiState.Data).hasMore)
    }

    @Test
    fun should_showLoadingMore_when_nextPageLoading() = runTest(testDispatcher) {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        assertTrue((viewModel.uiState.value as CardNewsListUiState.Data).isLoadingMore)
        advanceUntilIdle()
    }

    @Test
    fun should_appendCards_when_nextPageSuccess() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(20, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_revertLoadingMore_when_nextPageFails() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertFalse(state.isLoadingMore)
        assertEquals(10, state.cards.size)
    }

    @Test
    fun should_notLoadMore_when_noMorePages() = runTest {
        val cards = (1L..5L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardNewsListUiState.Data).hasMore)
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(5, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_notLoadMore_when_alreadyLoading() = runTest(testDispatcher) {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        // isLoadingMore=true at this point; second call should be blocked
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(20, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_resetPagination_when_filterChanged() = runTest {
        val cards = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        coEvery { getCardsByCustomerUseCase.invoke(any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        // After filter reset, cards come from getFiltered (page 0), 10 cards
        assertEquals(10, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_handleEmptyNextPage_when_noMoreCards() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertFalse(state.hasMore)
        assertEquals(10, state.cards.size)
    }

    @Test
    fun should_preserveExistingCards_when_loadingMore() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..15L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(15, state.cards.size)
        assertEquals(1L, state.cards.first().id)
        assertEquals(15L, state.cards.last().id)
    }

    // ===== ConversationType filter (23-32) =====

    @Test
    fun should_showAllCards_when_noFilterApplied() = runTest {
        val cards = listOf(
            makeCard(1, conversationType = ConversationType.CUSTOMER_MEETING),
            makeCard(2, conversationType = ConversationType.INTERNAL_MEETING)
        )
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(2, state.cards.size)
        assertNull(state.currentFilter)
    }

    @Test
    fun should_filterCustomerMeeting_when_typeSelected() = runTest {
        val customerCards = listOf(makeCard(1, conversationType = ConversationType.CUSTOMER_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(customerCards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(1, state.cards.size)
        assertEquals(ConversationType.CUSTOMER_MEETING, state.currentFilter)
    }

    @Test
    fun should_filterInternalMeeting_when_typeSelected() = runTest {
        val internalCards = listOf(makeCard(2, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(internalCards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(1, state.cards.size)
        assertEquals(ConversationType.INTERNAL_MEETING, state.currentFilter)
    }

    @Test
    fun should_showEmpty_when_filterMatchesNothing() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_resetToAll_when_filterCleared() = runTest {
        val allCards = listOf(makeCard(1), makeCard(2))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(allCards)
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(listOf(makeCard(1)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        viewModel.filterByType(null)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(2, state.cards.size)
        assertNull(state.currentFilter)
    }

    @Test
    fun should_showLoading_when_filterChanges() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_preserveFilter_when_dataState() = runTest {
        val cards = listOf(makeCard(1, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        assertEquals(ConversationType.INTERNAL_MEETING, (viewModel.uiState.value as CardNewsListUiState.Data).currentFilter)
    }

    @Test
    fun should_switchFilter_when_changingTypes() = runTest {
        val customerCards = listOf(makeCard(1, conversationType = ConversationType.CUSTOMER_MEETING))
        val internalCards = listOf(makeCard(2, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(customerCards)
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(internalCards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        assertEquals(ConversationType.CUSTOMER_MEETING, (viewModel.uiState.value as CardNewsListUiState.Data).currentFilter)
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        assertEquals(ConversationType.INTERNAL_MEETING, (viewModel.uiState.value as CardNewsListUiState.Data).currentFilter)
    }

    @Test
    fun should_resetPage_when_filterApplied() = runTest {
        val cards = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        coEvery { getCardsByCustomerUseCase.invoke(any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        // After filter, page is reset; only getFiltered result (10 cards)
        assertEquals(10, (viewModel.uiState.value as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_handleRapidFilterChanges_when_switching() = runTest {
        val cards = listOf(makeCard(1))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        viewModel.filterByType(null)
        advanceUntilIdle()
        // Final state should reflect null filter
        assertNull((viewModel.uiState.value as CardNewsListUiState.Data).currentFilter)
    }

    // ===== Navigation + error recovery (33-40) =====

    @Test
    fun should_emitNavigateEvent_when_cardClicked() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(1L)
        val event = viewModel.event.value
        assertTrue(event is CardNewsListEvent.NavigateToDetail)
        assertEquals(1L, (event as CardNewsListEvent.NavigateToDetail).cardId)
    }

    @Test
    fun should_clearNavigateEvent_when_consumed() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(1L)
        assertTrue(viewModel.event.value is CardNewsListEvent.NavigateToDetail)
        viewModel.onEventConsumed()
        assertNull(viewModel.event.value)
    }

    @Test
    fun should_recoverFromNetworkError_when_reloaded() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_recoverFromTimeoutError_when_reloaded() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.failure(DomainException.TimeoutException()),
            Result.success(listOf(makeCard()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Error)
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_showCorrectMessage_when_networkError() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("서버 연결에 실패했습니다", (viewModel.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showCorrectMessage_when_timeoutError() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (viewModel.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_handleRapidReloads_when_multipleCalls() = runTest {
        val cards = listOf(makeCard(1))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadCards()
        viewModel.loadCards()
        viewModel.loadCards()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_settleToFinalState_when_rapidLoads() = runTest {
        val cards = listOf(makeCard(1), makeCard(2))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        viewModel.loadCards()
        viewModel.loadCards()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(2, state.cards.size)
    }
}
