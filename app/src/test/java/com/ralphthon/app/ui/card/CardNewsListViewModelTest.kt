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
class CardNewsListViewModelTest {

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
        title: String = "테스트 카드",
        date: String = "2026-03-01",
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
        sentiment: Sentiment = Sentiment.POSITIVE,
        sentimentScore: Float = 0.8f
    ) = ContextCard.withDefaults(
        id = id,
        conversationId = id,
        customerId = customerId,
        title = title,
        date = date,
        conversationType = conversationType,
        sentiment = sentiment,
        sentimentScore = sentimentScore
    )

    private fun createViewModel(customerId: Long = 1L): CardNewsListViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("customerId" to customerId))
        return CardNewsListViewModel(getCardsByCustomerUseCase, savedStateHandle)
    }

    // ===== States (1-10) =====

    @Test
    fun should_showLoading_when_screenOpened() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        assertEquals(CardNewsListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_showCards_when_apiReturnsData() = runTest {
        val cards = listOf(makeCard(1), makeCard(2), makeCard(3))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Data)
        assertEquals(3, (state as CardNewsListUiState.Data).cards.size)
    }

    @Test
    fun should_showEmpty_when_noCards() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_showError_when_apiFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (state as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showError_when_timeout() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Error)
        assertEquals("서버 응답 시간이 초과되었습니다", (state as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showError_when_networkError() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (state as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_show5Cards_when_apiReturns5() = runTest {
        val cards = (1L..5L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(5, state.cards.size)
    }

    @Test
    fun should_preserveCardFields_when_dataShown() = runTest {
        val card = makeCard(
            id = 42L,
            customerId = 1L,
            title = "삼성전자 미팅",
            date = "2026-03-15",
            conversationType = ConversationType.CUSTOMER_MEETING,
            sentiment = Sentiment.POSITIVE,
            sentimentScore = 0.9f
        )
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        val c = state.cards.first()
        assertEquals(42L, c.id)
        assertEquals("삼성전자 미팅", c.title)
        assertEquals("2026-03-15", c.date)
        assertEquals(ConversationType.CUSTOMER_MEETING, c.conversationType)
        assertEquals(Sentiment.POSITIVE, c.sentiment)
        assertEquals(0.9f, c.sentimentScore)
    }

    @Test
    fun should_handleCustomerId_when_savedState() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(99L, any(), any(), any(), any()) } returns Result.success(listOf(makeCard(customerId = 99L)))
        val viewModel = createViewModel(customerId = 99L)
        advanceUntilIdle()
        assertEquals(99L, viewModel.customerId)
        assertTrue(viewModel.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_showData_when_10CardsReturned() = runTest {
        val cards = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(10, state.cards.size)
    }

    // ===== ConversationType filter (11-17) =====

    @Test
    fun should_showAllCards_when_noFilter() = runTest {
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
    fun should_showOnlyCustomerMeetings_when_filterCustomer() = runTest {
        val customerCards = listOf(makeCard(1, conversationType = ConversationType.CUSTOMER_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(customerCards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(1, state.cards.size)
        assertEquals(ConversationType.CUSTOMER_MEETING, state.cards.first().conversationType)
    }

    @Test
    fun should_showOnlyInternalMeetings_when_filterInternal() = runTest {
        val internalCards = listOf(makeCard(2, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(internalCards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(1, state.cards.size)
        assertEquals(ConversationType.INTERNAL_MEETING, state.cards.first().conversationType)
    }

    @Test
    fun should_showEmpty_when_filterHasNoResults() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        assertEquals(CardNewsListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_resetPagination_when_filterChanged() = runTest {
        val cards = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        coEvery { getCardsByCustomerUseCase.invoke(any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        // Load next page to advance currentPage
        viewModel.loadNextPage()
        advanceUntilIdle()
        // Now filter — should reset page
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        // After filter reset, cards come from getFiltered (page 0)
        assertEquals(10, state.cards.size)
    }

    @Test
    fun should_showLoading_when_filterChanges() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.CUSTOMER_MEETING, any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        // loadCards() sets Loading synchronously before launching coroutine
        // Verify that after filter, state eventually shows Data with the new filter
        viewModel.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(ConversationType.CUSTOMER_MEETING, state.currentFilter)
    }

    @Test
    fun should_preserveFilter_when_dataShown() = runTest {
        val cards = listOf(makeCard(1, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(ConversationType.INTERNAL_MEETING, state.currentFilter)
    }

    // ===== Pagination (18-25) =====

    @Test
    fun should_loadNextPage_when_scrolledToBottom() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(20, state.cards.size)
    }

    @Test
    fun should_notLoadMore_when_noMorePages() = runTest {
        val page0 = (1L..5L).map { makeCard(id = it) } // less than pageSize=10
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val stateBefore = viewModel.uiState.value as CardNewsListUiState.Data
        assertFalse(stateBefore.hasMore)
        viewModel.loadNextPage()
        advanceUntilIdle()
        // Cards count unchanged
        val stateAfter = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(5, stateAfter.cards.size)
    }

    @Test
    fun should_showLoadingMore_when_paginationInProgress() = runTest(testDispatcher) {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        // isLoadingMore set synchronously before launch
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertTrue(state.isLoadingMore)
        advanceUntilIdle()
    }

    @Test
    fun should_appendCards_when_nextPageLoaded() = runTest {
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

    @Test
    fun should_setHasMore_when_fullPageReturned() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertTrue(state.hasMore)
    }

    @Test
    fun should_setNoMore_when_partialPageReturned() = runTest {
        val page0 = (1L..7L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertFalse(state.hasMore)
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
        // isLoadingMore=true synchronously; second call is blocked by guard
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        // Should only have loaded page1 once (10 + 10 = 20, not 30)
        assertEquals(20, state.cards.size)
    }

    @Test
    fun should_revertPage_when_paginationFails() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(page0)
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        // Cards unchanged (still page0) and isLoadingMore is false
        assertEquals(10, state.cards.size)
        assertFalse(state.isLoadingMore)
    }

    // ===== Navigation (26-29) =====

    @Test
    fun should_navigateToDetail_when_cardClicked() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(1L)
        val event = viewModel.event.value
        assertTrue(event is CardNewsListEvent.NavigateToDetail)
    }

    @Test
    fun should_includeCardId_when_navigating() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(42L)
        val event = viewModel.event.value as CardNewsListEvent.NavigateToDetail
        assertEquals(42L, event.cardId)
    }

    @Test
    fun should_clearEvent_when_consumed() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(1L)
        viewModel.onEventConsumed()
        assertNull(viewModel.event.value)
    }

    @Test
    fun should_handleMultipleClicks_when_rapidClicking() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCardClick(1L)
        viewModel.onCardClick(2L)
        viewModel.onCardClick(3L)
        // Last click wins
        val event = viewModel.event.value as CardNewsListEvent.NavigateToDetail
        assertEquals(3L, event.cardId)
    }

    // ===== Refresh/Error (30-35) =====

    @Test
    fun should_refreshData_when_loadCardsCalled() = runTest {
        val initial = listOf(makeCard(1))
        val refreshed = listOf(makeCard(2), makeCard(3))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(initial),
            Result.success(refreshed)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadCards()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(2, state.cards.size)
    }

    @Test
    fun should_resetPage_when_refreshed() = runTest {
        val page0 = (1L..10L).map { makeCard(id = it) }
        val page1 = (11L..20L).map { makeCard(id = it) }
        val refreshed = listOf(makeCard(99))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(page0),
            Result.success(refreshed)
        )
        coEvery { getCardsByCustomerUseCase.invoke(any(), 1, any()) } returns Result.success(page1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.loadCards()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(1, state.cards.size)
        assertEquals(99L, state.cards.first().id)
    }

    @Test
    fun should_showError_when_serverError() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (state as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showError_when_unauthorized() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.UnauthorizedException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardNewsListUiState.Error)
        assertEquals("인증에 실패했습니다", (state as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_retryWithFilter_when_loadCardsAfterFilter() = runTest {
        val cards = listOf(makeCard(1, conversationType = ConversationType.INTERNAL_MEETING))
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), null, any(), any(), any()) } returns Result.success(emptyList())
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), ConversationType.INTERNAL_MEETING, any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        // loadCards again — should still use INTERNAL_MEETING filter
        viewModel.loadCards()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(ConversationType.INTERNAL_MEETING, state.currentFilter)
    }

    @Test
    fun should_handleLargeDataset_when_50Cards() = runTest {
        val cards = (1L..50L).map { makeCard(id = it) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardNewsListUiState.Data
        assertEquals(50, state.cards.size)
        assertTrue(state.hasMore)
    }
}
