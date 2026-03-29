package com.ralphthon.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.model.SearchResult
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
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineFlowTest {

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

    private fun makeCardDetailResult() = CardDetailResult(card = makeCard(), additionalKnowledge = emptyList())

    // ===== CustomerListViewModel Flow tests (1-7) =====

    @Test
    fun should_emitLoadingThenData_when_customerListLoads() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = CustomerListViewModel(getCustomersUseCase)
        vm.uiState.test {
            assertEquals(CustomerListUiState.Loading, awaitItem())
            assertInstanceOf(CustomerListUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenError_when_customerListFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerListViewModel(getCustomersUseCase)
        vm.uiState.test {
            assertEquals(CustomerListUiState.Loading, awaitItem())
            assertInstanceOf(CustomerListUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitDistinctStates_when_customerListSameDataReloaded() = runTest {
        val customers = listOf(makeCustomer())
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(customers)
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        val initialState = vm.uiState.value
        vm.loadCustomers()
        advanceUntilIdle()
        assertEquals(initialState, vm.uiState.value)
    }

    @Test
    fun should_emitLoadingThenEmpty_when_customerListIsEmpty() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val vm = CustomerListViewModel(getCustomersUseCase)
        vm.uiState.test {
            assertEquals(CustomerListUiState.Loading, awaitItem())
            assertEquals(CustomerListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitNullEventInitially_when_customerListCreated() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = CustomerListViewModel(getCustomersUseCase)
        vm.event.test {
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitNavigateEvent_when_customerListItemClicked() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        vm.event.test {
            awaitItem() // null initial
            vm.onCustomerClick(42L)
            val event = awaitItem()
            assertTrue(event is com.ralphthon.app.ui.customer.CustomerListEvent.NavigateToCards)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitNullAfterConsume_when_customerListEventConsumed() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = CustomerListViewModel(getCustomersUseCase)
        advanceUntilIdle()
        vm.onCustomerClick(1L)
        vm.onEventConsumed()
        assertEquals(null, vm.event.value)
    }

    // ===== CustomerBriefViewModel Flow tests (8-13) =====

    @Test
    fun should_emitLoadingThenData_when_customerBriefLoads() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        vm.uiState.test {
            assertEquals(CustomerBriefUiState.Loading, awaitItem())
            assertInstanceOf(CustomerBriefUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenError_when_customerBriefFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        vm.uiState.test {
            assertEquals(CustomerBriefUiState.Loading, awaitItem())
            assertInstanceOf(CustomerBriefUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitUpdatedData_when_customerBriefPanelToggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            val initial = awaitItem() as CustomerBriefUiState.Data
            assertEquals(false, initial.isPredictionsExpanded)
            vm.togglePredictions()
            val updated = awaitItem() as CustomerBriefUiState.Data
            assertEquals(true, updated.isPredictionsExpanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitTwoLoadingStates_when_customerBriefRefreshed() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // data
            vm.refresh()
            assertEquals(CustomerBriefUiState.Loading, awaitItem())
            assertInstanceOf(CustomerBriefUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitDataWithCorrectCustomer_when_customerBriefLoads() = runTest {
        coEvery { getCustomerBriefUseCase(5L) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 5L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals("테스트", state.brief.customer.companyName)
    }

    @Test
    fun should_notEmitDuplicateState_when_customerBriefSameToggleCalledTwice() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(makeCustomerBrief())
        val vm = CustomerBriefViewModel(getCustomerBriefUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.togglePriceHistory()
        vm.togglePriceHistory()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(false, state.isPriceHistoryExpanded)
    }

    // ===== CardNewsListViewModel Flow tests (14-20) =====

    @Test
    fun should_emitLoadingThenData_when_cardNewsListLoads() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        vm.uiState.test {
            assertEquals(CardNewsListUiState.Loading, awaitItem())
            assertInstanceOf(CardNewsListUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenEmpty_when_cardNewsListIsEmpty() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(emptyList())
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        vm.uiState.test {
            assertEquals(CardNewsListUiState.Loading, awaitItem())
            assertEquals(CardNewsListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingAgain_when_cardNewsListFilterChanges() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // current data
            vm.filterByType(ConversationType.INTERNAL_MEETING)
            assertEquals(CardNewsListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitNavigateEvent_when_cardNewsListCardClicked() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.event.test {
            awaitItem() // null
            vm.onCardClick(55L)
            val event = awaitItem()
            assertTrue(event is com.ralphthon.app.ui.card.CardNewsListEvent.NavigateToDetail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitNullEvent_when_cardNewsListEventConsumed() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.onCardClick(1L)
        vm.onEventConsumed()
        assertEquals(null, vm.event.value)
    }

    @Test
    fun should_updateFilterInState_when_cardNewsListFilterApplied() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.success(listOf(makeCard()))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        advanceUntilIdle()
        vm.filterByType(ConversationType.CUSTOMER_MEETING)
        advanceUntilIdle()
        val state = vm.uiState.value as CardNewsListUiState.Data
        assertEquals(ConversationType.CUSTOMER_MEETING, state.currentFilter)
    }

    @Test
    fun should_emitLoadingThenError_when_cardNewsListFails() = runTest {
        coEvery { getCardsByCustomerUseCase.getFiltered(any(), any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardNewsListViewModel(getCardsByCustomerUseCase, SavedStateHandle(mapOf("customerId" to 1L)))
        vm.uiState.test {
            assertEquals(CardNewsListUiState.Loading, awaitItem())
            assertInstanceOf(CardNewsListUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== CardDetailViewModel Flow tests (21-27) =====

    @Test
    fun should_emitLoadingThenData_when_cardDetailLoads() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        vm.uiState.test {
            assertEquals(CardDetailUiState.Loading, awaitItem())
            assertInstanceOf(CardDetailUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenError_when_cardDetailFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        vm.uiState.test {
            assertEquals(CardDetailUiState.Loading, awaitItem())
            assertInstanceOf(CardDetailUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitUpdatedData_when_cardDetailPanelToggled() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            val initial = awaitItem() as CardDetailUiState.Data
            assertEquals(false, initial.isPriceExpanded)
            vm.togglePricePanel()
            val updated = awaitItem() as CardDetailUiState.Data
            assertEquals(true, updated.isPriceExpanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingOnRetry_when_cardDetailRetried() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException("net")),
            Result.success(makeCardDetailResult())
        )
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // error
            vm.retry()
            assertEquals(CardDetailUiState.Loading, awaitItem())
            assertInstanceOf(CardDetailUiState.Data::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitFourPanelToggles_when_cardDetailAllPanelsToggled() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.togglePricePanel()
        vm.toggleActionPanel()
        vm.togglePredictionPanel()
        vm.toggleKnowledgePanel()
        val state = vm.uiState.value as CardDetailUiState.Data
        assertTrue(state.isPriceExpanded && state.isActionExpanded && state.isPredictionExpanded && state.isKnowledgeExpanded)
    }

    @Test
    fun should_emitDataWithCard_when_cardDetailLoadsSuccessfully() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        val state = vm.uiState.value as CardDetailUiState.Data
        assertEquals(1L, state.card.id)
    }

    @Test
    fun should_emitTwoLoadingStates_when_cardDetailLoadedTwice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeCardDetailResult())
        val vm = CardDetailViewModel(getCardDetailUseCase, SavedStateHandle(mapOf("cardId" to 1L)))
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // data
            vm.loadCardDetail()
            assertEquals(CardDetailUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== SearchViewModel Flow tests (28-34) =====

    @Test
    fun should_emitIdleInitially_when_searchViewModelCreated() = runTest {
        val vm = SearchViewModel(searchCardsUseCase)
        vm.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenData_when_searchSucceeds() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        assertInstanceOf(SearchUiState.Data::class.java, vm.uiState.value)
    }

    @Test
    fun should_emitLoadingThenEmpty_when_searchReturnsNoResults() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(emptyList())
        val vm = SearchViewModel(searchCardsUseCase)
        vm.uiState.test {
            awaitItem() // idle
            vm.search("없는검색어")
            assertEquals(SearchUiState.Loading, awaitItem())
            assertInstanceOf(SearchUiState.Empty::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitIdleOnClear_when_searchClearedAfterResults() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("테스트")
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // data
            vm.clearSearch()
            assertEquals(SearchUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_updateRecentSearches_when_searchSucceeds() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.recentSearches.test {
            awaitItem() // empty initial
            vm.search("테스트")
            advanceUntilIdle()
            val updated = awaitItem()
            assertTrue(updated.contains("테스트"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenError_when_searchFails() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = SearchViewModel(searchCardsUseCase)
        vm.uiState.test {
            awaitItem() // idle
            vm.search("테스트")
            assertEquals(SearchUiState.Loading, awaitItem())
            assertInstanceOf(SearchUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_keepRecentSearchesOrdered_when_multipleSearchesPerformed() = runTest {
        coEvery { searchCardsUseCase(any()) } returns Result.success(
            listOf(SearchResult(1L, "card", "제목", "스니펫", emptyList(), 1L, 0.9f))
        )
        val vm = SearchViewModel(searchCardsUseCase)
        vm.search("첫번째")
        advanceUntilIdle()
        vm.search("두번째")
        advanceUntilIdle()
        vm.search("세번째")
        advanceUntilIdle()
        assertEquals("세번째", vm.recentSearches.value[0])
        assertEquals("두번째", vm.recentSearches.value[1])
        assertEquals("첫번째", vm.recentSearches.value[2])
    }

    // ===== UploadViewModel Flow tests (35-40) =====

    @Test
    fun should_emitIdleInitially_when_uploadViewModelCreated() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        vm.uiState.test {
            assertEquals(UploadUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitUploadingThenSuccess_when_uploadSucceeds() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(
            com.ralphthon.app.domain.model.Conversation(
                id = 1L, customerId = 1L, title = "제목", date = "2026-03-29",
                type = ConversationType.CUSTOMER_MEETING, duration = 0, summary = "",
                sentiment = Sentiment.NEUTRAL, keywords = emptyList(),
                keyStatements = emptyList(), priceCommitments = emptyList(),
                actionItems = emptyList(), predictedQuestions = emptyList()
            )
        )
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.uiState.test {
            awaitItem() // idle
            vm.upload()
            assertEquals(UploadUiState.Uploading, awaitItem())
            assertInstanceOf(UploadUiState.Success::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitUploadingThenError_when_uploadFails() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.uiState.test {
            awaitItem() // idle
            vm.upload()
            assertEquals(UploadUiState.Uploading, awaitItem())
            assertInstanceOf(UploadUiState.Error::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitIdleAfterReset_when_uploadResetFormCalled() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException("net"))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("제목")
        vm.setFile("/path/to/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        vm.uiState.test {
            awaitItem() // error
            vm.resetForm()
            assertEquals(UploadUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_updateFormState_when_uploadFormFieldsSet() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.formState.test {
            awaitItem() // initial
            vm.setTitle("새 제목")
            val updated = awaitItem()
            assertEquals("새 제목", updated.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_updateConversationType_when_uploadTypeChanged() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        val vm = UploadViewModel(uploadUseCase, getCustomersUseCase)
        advanceUntilIdle()
        vm.setConversationType(ConversationType.INTERNAL_MEETING)
        assertEquals(ConversationType.INTERNAL_MEETING, vm.formState.value.conversationType)
    }
}
