package com.ralphthon.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.usecase.CardDetailResult
import com.ralphthon.app.domain.usecase.CustomerBrief
import com.ralphthon.app.domain.usecase.GetCardDetailUseCase
import com.ralphthon.app.domain.usecase.GetCardsByCustomerUseCase
import com.ralphthon.app.domain.usecase.GetCustomerBriefUseCase
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import com.ralphthon.app.domain.usecase.SearchCardsUseCase
import com.ralphthon.app.domain.usecase.UploadConversationUseCase
import com.ralphthon.app.ui.card.CardDetailUiState
import com.ralphthon.app.ui.card.CardDetailViewModel
import com.ralphthon.app.ui.card.CardNewsListUiState
import com.ralphthon.app.ui.card.CardNewsListViewModel
import com.ralphthon.app.ui.customer.CustomerBriefUiState
import com.ralphthon.app.ui.customer.CustomerBriefViewModel
import com.ralphthon.app.ui.customer.CustomerListUiState
import com.ralphthon.app.ui.customer.CustomerListViewModel
import com.ralphthon.app.ui.search.SearchUiState
import com.ralphthon.app.ui.search.SearchViewModel
import com.ralphthon.app.ui.upload.UploadUiState
import com.ralphthon.app.ui.upload.UploadViewModel
import io.mockk.coEvery
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelErrorTest {

    private val testDispatcher = StandardTestDispatcher()

    // --- mocks ---
    private val getCustomersUseCase: GetCustomersUseCase = mockk()
    private val getCustomerBriefUseCase: GetCustomerBriefUseCase = mockk()
    private val getCardsByCustomerUseCase: GetCardsByCustomerUseCase = mockk()
    private val getCardDetailUseCase: GetCardDetailUseCase = mockk()
    private val searchCardsUseCase: SearchCardsUseCase = mockk()
    private val uploadUseCase: UploadConversationUseCase = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== CustomerListViewModel (tests 1-10) =====

    @Test
    fun should_showNetworkError_when_customerListNetworkFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException("network"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showServerError_when_customerListServerFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (vm.uiState.value as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_customerListTimesOut() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showSnackbar_when_customerListRefreshFailsWithData() = runTest {
        val customers = listOf(Customer.withDefaults(id = 1L, name = "홍길동", company = "테스트"))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(customers),
            Result.failure(DomainException.NetworkException("net"))
        )
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
        assertTrue(vm.event.value != null)
    }

    @Test
    fun should_retryLoading_when_customerListRetryIsCalled() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(listOf(Customer.withDefaults(id = 1L, name = "홍길동", company = "테스트")))
        )
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_showUnauthorizedError_when_customerListAuthFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.UnauthorizedException())
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertEquals("인증에 실패했습니다", (vm.uiState.value as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showNotFoundError_when_customerListNotFound() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NotFoundException("customers"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertEquals("데이터를 찾을 수 없습니다", (vm.uiState.value as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_customerListUnexpectedFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(RuntimeException("unknown"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertEquals("알 수 없는 오류가 발생했습니다", (vm.uiState.value as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showErrorAfterRefreshFails_when_noDataExists() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
    }

    @Test
    fun should_clearSnackbarEvent_when_customerListEventConsumed() = runTest {
        val customers = listOf(Customer.withDefaults(id = 1L, name = "홍길동", company = "테스트"))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(customers),
            Result.failure(DomainException.NetworkException("net"))
        )
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        vm.onEventConsumed()
        assertEquals(null, vm.event.value)
    }

    // ===== CustomerBriefViewModel (tests 11-20) =====

    private fun makeCustomerBrief() = CustomerBrief(
        customer = Customer.withDefaults(id = 1L, name = "홍길동", company = "테스트"),
        lastConversationSummary = "요약",
        lastCustomerMeetingSummary = "고객 미팅 요약",
        lastInternalMeetingSummary = null,
        predictedQuestions = emptyList(),
        priceHistory = emptyList(),
        openActionItemsCount = 0,
        recentActionItems = emptyList(),
        overallSentiment = Sentiment.POSITIVE,
        totalCards = 1
    )

    @Test
    fun should_showNetworkError_when_customerBriefNetworkFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (vm.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_customerBriefTimesOut() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showServerError_when_customerBriefServerFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 오류가 발생했습니다", (vm.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showNotFoundError_when_customerBriefNotFound() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NotFoundException("customer"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("고객 정보를 찾을 수 없습니다", (vm.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_customerBriefUnexpectedFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(RuntimeException("unexpected"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("알 수 없는 오류가 발생했습니다", (vm.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_retryAndRecover_when_customerBriefRetrySucceeds() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(makeCustomerBrief())
        )
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_showLoadingThenError_when_customerBriefRefreshFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(makeCustomerBrief()),
            Result.failure(DomainException.ServerException(503, "unavailable"))
        )
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    @Test
    fun should_preserveCustomerId_when_customerBriefCreatedWithId() = runTest {
        coEvery { getCustomerBriefUseCase(42L) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 42L)))
        advanceUntilIdle()
        assertEquals(42L, vm.customerId)
    }

    @Test
    fun should_showErrorImmediately_when_customerBriefLoadFailsOnInit() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    @Test
    fun should_notUpdateState_when_customerBriefToggleCalledInErrorState() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.togglePredictions()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    // ===== CardNewsListViewModel (tests 21-30) =====

    private fun makeCard(id: Long = 1L, customerId: Long = 1L) = ContextCard.withDefaults(
        id = id, conversationId = id, customerId = customerId,
        title = "카드 $id", date = "2026-03-01", sentiment = Sentiment.POSITIVE, sentimentScore = 0.8f
    )

    @Test
    fun should_showNetworkError_when_cardNewsListNetworkFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardNewsListUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_cardNewsListTimesOut() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showServerError_when_cardNewsListServerFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 오류가 발생했습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_retryAndRecover_when_cardNewsListRetrySucceeds() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(listOf(makeCard()))
        )
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardNewsListUiState.Error)
        vm.loadCards()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardNewsListUiState.Data)
    }

    @Test
    fun should_showUnauthorizedError_when_cardNewsListAuthFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.UnauthorizedException())
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("인증에 실패했습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showNotFoundError_when_cardNewsListNotFound() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NotFoundException("cards"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("데이터를 찾을 수 없습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_cardNewsListUnexpectedFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(RuntimeException("unexpected"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertEquals("알 수 없는 오류가 발생했습니다", (vm.uiState.value as CardNewsListUiState.Error).message)
    }

    @Test
    fun should_revertLoadingMore_when_cardNewsListNextPageFails() = runTest {
        val cards = (1..10).map { makeCard(it.toLong()) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        coEvery { getCardsByCustomerUseCase(any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.loadNextPage()
        advanceUntilIdle()
        val state = vm.uiState.value as CardNewsListUiState.Data
        assertEquals(false, state.isLoadingMore)
    }

    @Test
    fun should_showErrorAfterFilterChange_when_filteredLoadFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.success(listOf(makeCard())),
            Result.failure(DomainException.ServerException(500, "err"))
        )
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.filterByType(ConversationType.INTERNAL_MEETING)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardNewsListUiState.Error)
    }

    @Test
    fun should_emitNavigateEvent_when_cardNewsListCardClicked() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.onCardClick(99L)
        assertEquals(99L, (vm.event.value as com.ralphthon.app.ui.card.CardNewsListEvent.NavigateToDetail).cardId)
    }

    // ===== CardDetailViewModel (tests 31-40) =====

    private fun makeCardDetailResult() = CardDetailResult(
        card = makeCard(),
        additionalKnowledge = emptyList()
    )

    @Test
    fun should_showNetworkError_when_cardDetailNetworkFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardDetailUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (vm.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_cardDetailTimesOut() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showNotFoundError_when_cardDetailNotFound() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NotFoundException("card"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertEquals("카드를 찾을 수 없습니다", (vm.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showServerError_when_cardDetailServerFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertEquals("서버 오류가 발생했습니다", (vm.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_cardDetailUnexpectedFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(RuntimeException("unexpected"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertEquals("알 수 없는 오류가 발생했습니다", (vm.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_retryAndRecover_when_cardDetailRetrySucceeds() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(makeCardDetailResult())
        )
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardDetailUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_notTogglePanel_when_cardDetailStateIsError() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.togglePricePanel()
        assertTrue(vm.uiState.value is CardDetailUiState.Error)
    }

    @Test
    fun should_showLoadingThenError_when_cardDetailReloadFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.success(makeCardDetailResult()),
            Result.failure(DomainException.ServerException(503, "unavailable"))
        )
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.loadCardDetail()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardDetailUiState.Error)
    }

    @Test
    fun should_preserveCardId_when_cardDetailCreatedWithId() = runTest {
        coEvery { getCardDetailUseCase(77L) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 77L)))
        advanceUntilIdle()
        assertEquals(77L, vm.cardId)
    }

    @Test
    fun should_returnToLoading_when_cardDetailRetryIsInvoked() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(makeCardDetailResult())
        )
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.retry()
        assertEquals(CardDetailUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
    }

    // ===== SearchViewModel (tests 41-50) =====

    @Test
    fun should_showNetworkError_when_searchNetworkFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (vm.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_searchTimesOut() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showServerError_when_searchServerFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertEquals("서버 오류가 발생했습니다", (vm.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_searchUnexpectedFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(RuntimeException("unexpected"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertEquals("검색 중 오류가 발생했습니다", (vm.uiState.value as SearchUiState.Error).message)
    }

    @Test
    fun should_goIdleOnClear_when_searchClearCalledAfterError() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
        vm.clearSearch()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notAddToRecentSearches_when_searchFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.isEmpty())
    }

    @Test
    fun should_remainIdle_when_searchQueryIsBlank() = runTest {
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("   ")
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_showErrorOnFilteredSearch_when_filteredSearchFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.ServerException(500, "err"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.searchFiltered("테스트", "CUSTOMER_MEETING")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
    }

    @Test
    fun should_cancelPreviousSearch_when_newSearchIssued() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("첫번째")
        vm.search("두번째")
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is SearchUiState.Empty)
        assertEquals("두번째", (state as SearchUiState.Empty).query)
    }

    @Test
    fun should_removeRecentSearch_when_removeCalledOnExistingEntry() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.contains("테스트"))
        vm.removeRecentSearch("테스트")
        assertTrue(vm.recentSearches.value.isEmpty())
    }

    // ===== UploadViewModel (tests 51-60) =====

    private fun makeCustomer(id: Long = 1L) = Customer.withDefaults(id = id, name = "홍길동", company = "테스트")

    @Test
    fun should_showNetworkError_when_uploadNetworkFails() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (vm.uiState.value as UploadUiState.Error).message)
    }

    @Test
    fun should_showTimeoutError_when_uploadTimesOut() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (vm.uiState.value as UploadUiState.Error).message)
    }

    @Test
    fun should_showUnknownError_when_uploadUnexpectedFails() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(RuntimeException("unexpected"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals("업로드 중 오류가 발생했습니다", (vm.uiState.value as UploadUiState.Error).message)
    }

    @Test
    fun should_notUpload_when_customerNotSelected() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notUpload_when_titleIsBlank() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notUpload_when_fileNotSelected() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_resetToIdle_when_uploadResetFormCalled() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        vm.resetForm()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_loadCustomersOnInit_when_uploadViewModelCreated() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer(1L), makeCustomer(2L)))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        assertEquals(2, vm.formState.value.customers.size)
    }

    @Test
    fun should_keepIdleState_when_customerLoadFails() = runTest {
        coEvery { getCustomersUseCase() } returns Result.failure(DomainException.NetworkException("net"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_showSuccessState_when_uploadSucceeds() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(
            com.ralphthon.app.domain.model.Conversation(
                id = 1L, customerId = 1L, title = "제목",
                date = "2026-03-29", type = ConversationType.CUSTOMER_MEETING,
                duration = 0, summary = "", sentiment = Sentiment.NEUTRAL,
                keywords = emptyList(), keyStatements = emptyList(),
                priceCommitments = emptyList(), actionItems = emptyList(),
                predictedQuestions = emptyList()
            )
        )
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Success)
    }
}
