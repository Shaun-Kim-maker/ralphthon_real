package com.ralphthon.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
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
import com.ralphthon.app.ui.upload.UploadFormState
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
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
class ParameterizedStateTest {

    private val testDispatcher = StandardTestDispatcher()

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

    // ===== helpers =====

    private fun makeCard(id: Long = 1L, customerId: Long = 1L) = ContextCard.withDefaults(
        id = id, conversationId = id, customerId = customerId,
        title = "카드 $id", date = "2026-03-01", sentiment = Sentiment.POSITIVE, sentimentScore = 0.8f
    )

    private fun makeCustomer(id: Long = 1L) =
        Customer.withDefaults(id = id, name = "홍길동", company = "테스트")

    private fun makeCustomerBrief() = CustomerBrief(
        customer = makeCustomer(),
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

    companion object {
        @JvmStatic
        fun networkErrors(): List<Throwable> = listOf(
            DomainException.NetworkException("network"),
            DomainException.TimeoutException(),
            DomainException.ServerException(500, "server error"),
            DomainException.UnauthorizedException(),
            DomainException.NotFoundException("resource")
        )

        @JvmStatic
        fun errorMessages(): List<Array<Any>> = listOf(
            arrayOf(DomainException.NetworkException("net"), "서버 연결에 실패했습니다"),
            arrayOf(DomainException.TimeoutException(), "서버 응답 시간이 초과되었습니다"),
            arrayOf(DomainException.ServerException(500, "err"), "서버 오류가 발생했습니다"),
            arrayOf(RuntimeException("unknown"), "알 수 없는 오류가 발생했습니다")
        )

        @JvmStatic
        fun searchErrorMessages(): List<Array<Any>> = listOf(
            arrayOf(DomainException.NetworkException("net"), "서버 연결에 실패했습니다"),
            arrayOf(DomainException.TimeoutException(), "서버 응답 시간이 초과되었습니다"),
            arrayOf(DomainException.ServerException(500, "err"), "서버 오류가 발생했습니다"),
            arrayOf(RuntimeException("unknown"), "검색 중 오류가 발생했습니다")
        )

        @JvmStatic
        fun customerCounts(): List<Int> = listOf(1, 3, 5, 10, 20)

        @JvmStatic
        fun cardCounts(): List<Int> = listOf(1, 5, 9, 10, 15)

        @JvmStatic
        fun conversationTypes(): List<ConversationType> = listOf(
            ConversationType.CUSTOMER_MEETING,
            ConversationType.INTERNAL_MEETING
        )

        @JvmStatic
        fun customerIds(): List<Long> = listOf(1L, 5L, 42L, 100L, 999L)

        @JvmStatic
        fun cardIds(): List<Long> = listOf(1L, 2L, 10L, 50L, 100L)

        @JvmStatic
        fun briefErrorMessages(): List<Array<Any>> = listOf(
            arrayOf(DomainException.NetworkException("net"), "서버 연결에 실패했습니다"),
            arrayOf(DomainException.TimeoutException(), "서버 응답 시간이 초과되었습니다"),
            arrayOf(DomainException.NotFoundException("customer"), "고객 정보를 찾을 수 없습니다"),
            arrayOf(DomainException.ServerException(500, "err"), "서버 오류가 발생했습니다"),
            arrayOf(RuntimeException("unknown"), "알 수 없는 오류가 발생했습니다")
        )

        @JvmStatic
        fun cardDetailErrorMessages(): List<Array<Any>> = listOf(
            arrayOf(DomainException.NetworkException("net"), "서버 연결에 실패했습니다"),
            arrayOf(DomainException.TimeoutException(), "서버 응답 시간이 초과되었습니다"),
            arrayOf(DomainException.NotFoundException("card"), "카드를 찾을 수 없습니다"),
            arrayOf(DomainException.ServerException(500, "err"), "서버 오류가 발생했습니다"),
            arrayOf(RuntimeException("unknown"), "알 수 없는 오류가 발생했습니다")
        )
    }

    // ===== CustomerListViewModel Parameterized (tests 1-10) =====

    @ParameterizedTest
    @MethodSource("errorMessages")
    fun should_showCorrectErrorMessage_when_customerListFails(error: Throwable, expectedMsg: String) = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(error)
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Error
        assertEquals(expectedMsg, state.message)
    }

    @ParameterizedTest
    @MethodSource("customerCounts")
    fun should_showAllCustomers_when_customerListLoadsMultiple(count: Int) = runTest {
        val customers = (1..count).map { makeCustomer(it.toLong()) }
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(customers)
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Data
        assertEquals(count, state.customers.size)
    }

    @ParameterizedTest
    @ValueSource(strings = ["LAST_INTERACTION", "NAME", "CONVERSATIONS"])
    fun should_loadCustomers_when_differentSortByUsed(sortByName: String) = runTest {
        val sortBy = GetCustomersUseCase.SortBy.valueOf(sortByName)
        coEvery { getCustomersUseCase.getSorted(sortBy) } returns Result.success(listOf(makeCustomer()))
        val vm = CustomerListViewModel(getCustomersUseCase)
        vm.sortBy(sortBy)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @ParameterizedTest
    @MethodSource("networkErrors")
    fun should_showErrorState_when_customerListThrowsAnyNetworkError(error: Throwable) = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(error)
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
    }

    // ===== CustomerBriefViewModel Parameterized (tests 11-20) =====

    @ParameterizedTest
    @MethodSource("briefErrorMessages")
    fun should_showCorrectErrorMessage_when_customerBriefFails(error: Throwable, expectedMsg: String) = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(error)
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Error
        assertEquals(expectedMsg, state.message)
    }

    @ParameterizedTest
    @MethodSource("customerIds")
    fun should_useCorrectCustomerId_when_customerBriefCreatedWithVariousIds(customerId: Long) = runTest {
        coEvery { getCustomerBriefUseCase(customerId) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to customerId)))
        advanceUntilIdle()
        assertEquals(customerId, vm.customerId)
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5])
    fun should_togglePanelCorrectly_when_customerBriefPanelToggledNTimes(times: Int) = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        repeat(times) { vm.togglePredictions() }
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(times % 2 == 1, state.isPredictionsExpanded)
    }

    @ParameterizedTest
    @MethodSource("networkErrors")
    fun should_showErrorState_when_customerBriefThrowsAnyNetworkError(error: Throwable) = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(error)
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    // ===== CardNewsListViewModel Parameterized (tests 21-30) =====

    @ParameterizedTest
    @MethodSource("errorMessages")
    fun should_showCorrectErrorMessage_when_cardNewsListFails(error: Throwable, expectedMsg: String) = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(error)
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CardNewsListUiState.Error
        assertEquals(expectedMsg, state.message)
    }

    @ParameterizedTest
    @MethodSource("cardCounts")
    fun should_showCorrectCardCount_when_cardNewsListLoadsMultiple(count: Int) = runTest {
        val cards = (1..count).map { makeCard(it.toLong()) }
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(cards)
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CardNewsListUiState.Data
        assertEquals(count, state.cards.size)
    }

    @ParameterizedTest
    @MethodSource("conversationTypes")
    fun should_applyFilter_when_cardNewsListFilteredByConversationType(type: ConversationType) = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.filterByType(type)
        advanceUntilIdle()
        val state = vm.uiState.value as CardNewsListUiState.Data
        assertEquals(type, state.currentFilter)
    }

    @ParameterizedTest
    @MethodSource("networkErrors")
    fun should_showErrorState_when_cardNewsListThrowsAnyNetworkError(error: Throwable) = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(error)
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardNewsListUiState.Error)
    }

    // ===== CardDetailViewModel Parameterized (tests 31-40) =====

    @ParameterizedTest
    @MethodSource("cardDetailErrorMessages")
    fun should_showCorrectErrorMessage_when_cardDetailFails(error: Throwable, expectedMsg: String) = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(error)
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CardDetailUiState.Error
        assertEquals(expectedMsg, state.message)
    }

    @ParameterizedTest
    @MethodSource("cardIds")
    fun should_useCorrectCardId_when_cardDetailCreatedWithVariousIds(cardId: Long) = runTest {
        coEvery { getCardDetailUseCase(cardId) } returns Result.success(CardDetailResult(makeCard(cardId), emptyList()))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to cardId)))
        advanceUntilIdle()
        assertEquals(cardId, vm.cardId)
        assertTrue(vm.uiState.value is CardDetailUiState.Data)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun should_togglePricePanel_when_cardDetailPanelToggledNTimes(times: Int) = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(CardDetailResult(makeCard(), emptyList()))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        repeat(times) { vm.togglePricePanel() }
        val state = vm.uiState.value as CardDetailUiState.Data
        assertEquals(times % 2 == 1, state.isPriceExpanded)
    }

    @ParameterizedTest
    @MethodSource("networkErrors")
    fun should_showErrorState_when_cardDetailThrowsAnyNetworkError(error: Throwable) = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(error)
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CardDetailUiState.Error)
    }

    // ===== SearchViewModel Parameterized (tests 41-50) =====

    @ParameterizedTest
    @MethodSource("searchErrorMessages")
    fun should_showCorrectErrorMessage_when_searchFails(error: Throwable, expectedMsg: String) = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(error)
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Error
        assertEquals(expectedMsg, state.message)
    }

    @ParameterizedTest
    @ValueSource(strings = ["  ", "\t", "\n", "   테스트   "])
    fun should_trimQuery_when_searchCalledWithWhitespace(query: String) = runTest {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            val vm = SearchViewModel(searchCardsUseCase)
            vm.search(query)
            advanceUntilIdle()
            assertEquals(SearchUiState.Idle, vm.uiState.value)
        } else {
            coEvery { searchCardsUseCase(trimmed) } returns Result.success(emptyList())
            val vm = SearchViewModel(searchCardsUseCase)
            vm.search(query)
            advanceUntilIdle()
            assertTrue(vm.uiState.value is SearchUiState.Empty)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["고객", "미팅", "가격", "계약", "할인"])
    fun should_addToRecentSearches_when_searchSucceedsWithVariousQueries(query: String) = runTest {
        coEvery { searchCardsUseCase(query) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search(query)
        advanceUntilIdle()
        assertTrue(vm.recentSearches.value.contains(query))
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3, 5, 7, 10])
    fun should_returnCorrectResultCount_when_searchReturnsMultipleResults(count: Int) = runTest {
        val results = (1..count).map {
            SearchResult(it.toLong(), "card", "제목 $it", "스니펫 $it", emptyList(), it.toLong(), 0.9f)
        }
        coEvery { searchCardsUseCase(any()) } returns Result.success(results)
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        val state = vm.uiState.value as SearchUiState.Data
        assertEquals(count, state.results.size)
    }

    @ParameterizedTest
    @MethodSource("networkErrors")
    fun should_showErrorState_when_searchThrowsAnyNetworkError(error: Throwable) = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(error)
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)
    }
}
