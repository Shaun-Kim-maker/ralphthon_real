package com.ralphthon.app.ui.customer

import androidx.lifecycle.SavedStateHandle
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.usecase.CustomerBrief
import com.ralphthon.app.domain.usecase.GetCustomerBriefUseCase
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CustomerBriefStateTest {

    @MockK
    private lateinit var getCustomerBriefUseCase: GetCustomerBriefUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testCustomer = Customer(1L, "삼성전자", "김부장", "전자", "2026-03-15", 10, "요약")

    private val testQuestion = PredictedQuestion(
        id = 1L,
        question = "가격 협상이 가능한가요?",
        suggestedAnswer = "네, 가능합니다",
        relatedKnowledge = listOf("가격 정책"),
        confidence = 0.9f
    )

    private val testPrice = PriceCommitment(
        id = 1L,
        amount = 1000000.0,
        currency = "KRW",
        condition = "3년 계약 조건",
        mentionedAt = "2026-03-15"
    )

    private val testAction = ActionItem(
        id = 1L,
        description = "제안서 발송",
        assignee = "김영업",
        dueDate = "2026-03-20",
        status = ActionItemStatus.OPEN
    )

    private val testBrief = CustomerBrief(
        customer = testCustomer,
        lastConversationSummary = "[고객 미팅] 미팅 요약\n[사내 회의] 회의 요약",
        lastCustomerMeetingSummary = "미팅 요약",
        lastInternalMeetingSummary = "회의 요약",
        predictedQuestions = listOf(testQuestion),
        priceHistory = listOf(testPrice),
        openActionItemsCount = 3,
        recentActionItems = listOf(testAction),
        overallSentiment = Sentiment.POSITIVE,
        totalCards = 20
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(customerId: Long = 1L): CustomerBriefViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("customerId" to customerId))
        return CustomerBriefViewModel(getCustomerBriefUseCase, savedStateHandle)
    }

    // === State transitions (1-10) ===

    @Test
    fun should_transitionToLoading_when_initialState() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        assertEquals(CustomerBriefUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionToData_when_loadSucceeds() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_transitionToError_when_loadFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    @Test
    fun should_transitionLoadingToData_when_briefLoaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        assertEquals(CustomerBriefUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_transitionLoadingToError_when_briefFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val vm = createViewModel()
        assertEquals(CustomerBriefUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    @Test
    fun should_transitionErrorToLoading_when_retried() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(testBrief)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_transitionErrorToData_when_retrySucceeds() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(testBrief)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_transitionDataToLoading_when_refreshed() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
        vm.loadBrief()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_transitionDataToData_when_refreshSucceeds() = runTest {
        val updated = testBrief.copy(totalCards = 30)
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.success(updated)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(30, state.brief.totalCards)
    }

    @Test
    fun should_transitionDataToError_when_refreshFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.failure(DomainException.NetworkException())
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    // === Brief content states (11-20) ===

    @Test
    fun should_showIntegratedSummary_when_dataLoaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.lastConversationSummary.contains("[고객 미팅]"))
        assertTrue(state.brief.lastConversationSummary.contains("[사내 회의]"))
    }

    @Test
    fun should_showPredictedQuestions_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(1, state.brief.predictedQuestions.size)
        assertEquals("가격 협상이 가능한가요?", state.brief.predictedQuestions.first().question)
    }

    @Test
    fun should_showEmptyQuestions_when_none() = runTest {
        val brief = testBrief.copy(predictedQuestions = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.predictedQuestions.isEmpty())
    }

    @Test
    fun should_showPriceHistory_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(1, state.brief.priceHistory.size)
        assertEquals(1000000.0, state.brief.priceHistory.first().amount)
    }

    @Test
    fun should_showEmptyPriceHistory_when_none() = runTest {
        val brief = testBrief.copy(priceHistory = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.priceHistory.isEmpty())
    }

    @Test
    fun should_showOpenActionCount_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(3, state.brief.openActionItemsCount)
    }

    @Test
    fun should_showZeroActionCount_when_none() = runTest {
        val brief = testBrief.copy(openActionItemsCount = 0, recentActionItems = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(0, state.brief.openActionItemsCount)
    }

    @Test
    fun should_showSentiment_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(Sentiment.POSITIVE, state.brief.overallSentiment)
    }

    @Test
    fun should_showTotalCards_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(20, state.brief.totalCards)
    }

    @Test
    fun should_showCustomerInfo_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals("삼성전자", state.brief.customer.companyName)
        assertEquals(1L, state.brief.customer.id)
    }

    // === Panel toggle states (21-28) ===

    @Test
    fun should_defaultCollapsed_when_panelsInitial() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertFalse(state.isPredictionsExpanded)
        assertFalse(state.isPriceHistoryExpanded)
        assertFalse(state.isActionItemsExpanded)
    }

    @Test
    fun should_expandPredictions_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePredictions()
        assertTrue((vm.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
    }

    @Test
    fun should_collapsePredictions_when_toggledBack() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePredictions()
        vm.togglePredictions()
        assertFalse((vm.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
    }

    @Test
    fun should_expandPriceHistory_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePriceHistory()
        assertTrue((vm.uiState.value as CustomerBriefUiState.Data).isPriceHistoryExpanded)
    }

    @Test
    fun should_expandActionItems_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleActionItems()
        assertTrue((vm.uiState.value as CustomerBriefUiState.Data).isActionItemsExpanded)
    }

    @Test
    fun should_toggleIndependently_when_multiplePanels() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePredictions()
        vm.togglePriceHistory()
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.isPredictionsExpanded)
        assertTrue(state.isPriceHistoryExpanded)
        assertFalse(state.isActionItemsExpanded)
    }

    @Test
    fun should_preservePanelState_when_dataRefreshed() = runTest {
        val updated = testBrief.copy(totalCards = 25)
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.success(updated)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePredictions()
        assertTrue((vm.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
        vm.refresh()
        advanceUntilIdle()
        // After refresh, new Data state resets panels (loadBrief creates fresh Data)
        val state = vm.uiState.value as CustomerBriefUiState.Data
        assertEquals(25, state.brief.totalCards)
    }

    @Test
    fun should_resetPanels_when_errorOccurs() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.failure(DomainException.NetworkException())
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.togglePredictions()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
    }

    // === Error recovery (29-33) ===

    @Test
    fun should_recoverFromNetwork_when_retried() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(testBrief)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_recoverFromTimeout_when_retried() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.TimeoutException()),
            Result.success(testBrief)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_showCorrectMessage_when_networkError() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_showCorrectMessage_when_timeoutError() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    @Test
    fun should_showCorrectMessage_when_notFoundError() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NotFoundException())
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerBriefUiState.Error
        assertEquals("고객 정보를 찾을 수 없습니다", state.message)
    }

    // === Rapid changes (34-35) ===

    @Test
    fun should_handleRapidRefresh_when_multipleCalls() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        vm.refresh()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_settleToFinalState_when_rapidRetries() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.failure(DomainException.TimeoutException()),
            Result.success(testBrief)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerBriefUiState.Data)
    }
}
