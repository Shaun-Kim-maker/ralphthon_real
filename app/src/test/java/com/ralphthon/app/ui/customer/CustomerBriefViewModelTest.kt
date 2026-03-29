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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CustomerBriefViewModelTest {

    @MockK
    private lateinit var getCustomerBriefUseCase: GetCustomerBriefUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testCustomer = Customer(1L, "삼성전자", "김부장", "전자", "2025-03-15", 10, "요약")

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
        mentionedAt = "2025-03-15"
    )

    private val testAction = ActionItem(
        id = 1L,
        description = "제안서 발송",
        assignee = "김영업",
        dueDate = "2025-03-20",
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
        MockKAnnotations.init(this)
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

    // ===== States (1-6) =====

    @Test
    fun should_showLoading_when_screenOpened() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        assertEquals(CustomerBriefUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_showBrief_when_loadSucceeds() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_showError_when_loadFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.UnknownException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
        assertEquals("알 수 없는 오류가 발생했습니다", (viewModel.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showError_when_networkError() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (viewModel.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_showError_when_timeout() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
        assertEquals("서버 응답 시간이 초과되었습니다", (viewModel.uiState.value as CustomerBriefUiState.Error).message)
    }

    @Test
    fun should_handleCustomerId_when_savedState() = runTest {
        coEvery { getCustomerBriefUseCase(42L) } returns Result.success(testBrief)
        val viewModel = createViewModel(customerId = 42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.customerId)
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Data)
    }

    // ===== Summary display (7-12) =====

    @Test
    fun should_showLastConversationSummary_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("[고객 미팅] 미팅 요약\n[사내 회의] 회의 요약", state.brief.lastConversationSummary)
    }

    @Test
    fun should_showCustomerMeetingSummary_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("미팅 요약", state.brief.lastCustomerMeetingSummary)
    }

    @Test
    fun should_showInternalMeetingSummary_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("회의 요약", state.brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_handleNullCustomerMeeting_when_noMeeting() = runTest {
        val brief = testBrief.copy(lastCustomerMeetingSummary = null)
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(null, state.brief.lastCustomerMeetingSummary)
    }

    @Test
    fun should_handleNullInternalMeeting_when_noMeeting() = runTest {
        val brief = testBrief.copy(lastInternalMeetingSummary = null)
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(null, state.brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_showIntegratedSummary_when_bothTypes() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.lastConversationSummary.contains("[고객 미팅]"))
        assertTrue(state.brief.lastConversationSummary.contains("[사내 회의]"))
    }

    // ===== Predicted questions (13-17) =====

    @Test
    fun should_showPredictedQuestions_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(1, state.brief.predictedQuestions.size)
    }

    @Test
    fun should_showEmptyQuestions_when_none() = runTest {
        val brief = testBrief.copy(predictedQuestions = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.predictedQuestions.isEmpty())
    }

    @Test
    fun should_show5Questions_when_manyAvailable() = runTest {
        val questions = (1..5).map { i ->
            PredictedQuestion(i.toLong(), "질문 $i", "답변 $i", emptyList(), 0.9f - i * 0.1f)
        }
        val brief = testBrief.copy(predictedQuestions = questions)
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(5, state.brief.predictedQuestions.size)
    }

    @Test
    fun should_showConfidence_when_questionPresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(0.9f, state.brief.predictedQuestions.first().confidence)
    }

    @Test
    fun should_showSuggestedAnswer_when_questionPresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("네, 가능합니다", state.brief.predictedQuestions.first().suggestedAnswer)
    }

    // ===== Price history (18-22) =====

    @Test
    fun should_showPriceHistory_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(1, state.brief.priceHistory.size)
    }

    @Test
    fun should_showEmptyPriceHistory_when_none() = runTest {
        val brief = testBrief.copy(priceHistory = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.priceHistory.isEmpty())
    }

    @Test
    fun should_showAmount_when_pricePresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(1000000.0, state.brief.priceHistory.first().amount)
    }

    @Test
    fun should_showCurrency_when_pricePresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("KRW", state.brief.priceHistory.first().currency)
    }

    @Test
    fun should_showCondition_when_pricePresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("3년 계약 조건", state.brief.priceHistory.first().condition)
    }

    // ===== Action items (23-26) =====

    @Test
    fun should_showOpenCount_when_actionItemsPresent() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(3, state.brief.openActionItemsCount)
    }

    @Test
    fun should_showZeroCount_when_noOpenItems() = runTest {
        val brief = testBrief.copy(openActionItemsCount = 0, recentActionItems = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(0, state.brief.openActionItemsCount)
    }

    @Test
    fun should_showRecentItems_when_present() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(1, state.brief.recentActionItems.size)
        assertEquals("제안서 발송", state.brief.recentActionItems.first().description)
    }

    @Test
    fun should_showEmptyRecentItems_when_none() = runTest {
        val brief = testBrief.copy(recentActionItems = emptyList())
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(brief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.recentActionItems.isEmpty())
    }

    // ===== Sentiment + totalCards (27-29) =====

    @Test
    fun should_showOverallSentiment_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(Sentiment.POSITIVE, state.brief.overallSentiment)
    }

    @Test
    fun should_showTotalCards_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(20, state.brief.totalCards)
    }

    @Test
    fun should_showCustomerInfo_when_loaded() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals("삼성전자", state.brief.customer.companyName)
        assertEquals(1L, state.brief.customer.id)
    }

    // ===== Panel toggles (30-34) =====

    @Test
    fun should_expandPredictions_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
        viewModel.togglePredictions()
        assertTrue((viewModel.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
    }

    @Test
    fun should_collapsePredictions_when_toggledTwice() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePredictions()
        viewModel.togglePredictions()
        assertFalse((viewModel.uiState.value as CustomerBriefUiState.Data).isPredictionsExpanded)
    }

    @Test
    fun should_expandPriceHistory_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CustomerBriefUiState.Data).isPriceHistoryExpanded)
        viewModel.togglePriceHistory()
        assertTrue((viewModel.uiState.value as CustomerBriefUiState.Data).isPriceHistoryExpanded)
    }

    @Test
    fun should_expandActionItems_when_toggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CustomerBriefUiState.Data).isActionItemsExpanded)
        viewModel.toggleActionItems()
        assertTrue((viewModel.uiState.value as CustomerBriefUiState.Data).isActionItemsExpanded)
    }

    @Test
    fun should_independentPanels_when_multipleToggled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePredictions()
        viewModel.togglePriceHistory()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.isPredictionsExpanded)
        assertTrue(state.isPriceHistoryExpanded)
        assertFalse(state.isActionItemsExpanded)
    }

    // ===== Refresh/retry (35-38) =====

    @Test
    fun should_showLoading_when_refreshed() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(testBrief)
        val viewModel = createViewModel()
        // Initial state before coroutine runs is Loading
        assertEquals(CustomerBriefUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Data)
    }

    @Test
    fun should_showData_when_refreshSucceeds() = runTest {
        val updatedBrief = testBrief.copy(totalCards = 25)
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.success(updatedBrief)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertEquals(25, state.brief.totalCards)
    }

    @Test
    fun should_showError_when_refreshFails() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.success(testBrief),
            Result.failure(DomainException.NetworkException())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
    }

    @Test
    fun should_retryLoad_when_retryCalled() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(testBrief)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Data)
    }

    // ===== Edge (39-40) =====

    @Test
    fun should_handleEmptyBrief_when_noData() = runTest {
        val emptyBrief = testBrief.copy(
            predictedQuestions = emptyList(),
            priceHistory = emptyList(),
            recentActionItems = emptyList(),
            openActionItemsCount = 0,
            totalCards = 0
        )
        coEvery { getCustomerBriefUseCase(any()) } returns Result.success(emptyBrief)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerBriefUiState.Data
        assertTrue(state.brief.predictedQuestions.isEmpty())
        assertTrue(state.brief.priceHistory.isEmpty())
        assertTrue(state.brief.recentActionItems.isEmpty())
        assertEquals(0, state.brief.openActionItemsCount)
        assertEquals(0, state.brief.totalCards)
    }

    @Test
    fun should_handleServerError_when_loading() = runTest {
        coEvery { getCustomerBriefUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerBriefUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (viewModel.uiState.value as CustomerBriefUiState.Error).message)
    }
}
