package com.ralphthon.app.ui.card

import androidx.lifecycle.SavedStateHandle
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.KeyStatement
import com.ralphthon.app.domain.model.Keyword
import com.ralphthon.app.domain.model.KeywordCategory
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.usecase.CardDetailResult
import com.ralphthon.app.domain.usecase.GetCardDetailUseCase
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
class CardDetailStateTest {

    @MockK
    private lateinit var getCardDetailUseCase: GetCardDetailUseCase

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
        keywords: List<Keyword> = emptyList(),
        keyStatements: List<KeyStatement> = emptyList(),
        priceCommitments: List<PriceCommitment> = emptyList(),
        actionItems: List<ActionItem> = emptyList(),
        predictedQuestions: List<PredictedQuestion> = emptyList(),
        relatedKnowledge: List<KnowledgeArticle> = emptyList(),
        sentiment: Sentiment = Sentiment.POSITIVE,
        sentimentScore: Float = 0.8f,
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING
    ) = ContextCard(
        id = id,
        conversationId = 10L,
        customerId = 100L,
        title = "테스트 카드",
        date = "2026-03-29",
        conversationType = conversationType,
        summary = "요약",
        sentiment = sentiment,
        sentimentScore = sentimentScore,
        keywords = keywords,
        keyStatements = keyStatements,
        priceCommitments = priceCommitments,
        actionItems = actionItems,
        predictedQuestions = predictedQuestions,
        relatedKnowledge = relatedKnowledge
    )

    private fun makeResult(
        card: ContextCard = makeCard(),
        additionalKnowledge: List<KnowledgeArticle> = emptyList()
    ) = CardDetailResult(card = card, additionalKnowledge = additionalKnowledge)

    private fun createViewModel(cardId: Long = 1L): CardDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("cardId" to cardId))
        return CardDetailViewModel(getCardDetailUseCase, savedStateHandle)
    }

    // ===== State transitions (1-10) =====

    @Test
    fun should_startWithLoading_when_created() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionToData_when_cardLoaded() = runTest {
        val card = makeCard(id = 1L)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
        assertEquals(card, (viewModel.uiState.value as CardDetailUiState.Data).card)
    }

    @Test
    fun should_transitionToError_when_loadFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
    }

    @Test
    fun should_transitionLoadingToData_when_success() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_transitionLoadingToError_when_failure() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
    }

    @Test
    fun should_transitionErrorToLoading_when_retried() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
        viewModel.retry()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionErrorToData_when_retrySucceeds() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_transitionDataToLoading_when_reloaded() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.success(makeResult()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
        viewModel.loadCardDetail()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionDataToData_when_refreshSucceeds() = runTest {
        val card1 = makeCard(id = 1L)
        val card2 = makeCard(id = 2L)
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.success(makeResult(card = card1)),
            Result.success(makeResult(card = card2))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1L, (viewModel.uiState.value as CardDetailUiState.Data).card.id)
        viewModel.loadCardDetail()
        advanceUntilIdle()
        assertEquals(2L, (viewModel.uiState.value as CardDetailUiState.Data).card.id)
    }

    @Test
    fun should_transitionDataToError_when_refreshFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.success(makeResult()),
            Result.failure(DomainException.NetworkException())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
        viewModel.loadCardDetail()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
    }

    // ===== Panel state transitions (11-26) =====

    @Test
    fun should_defaultAllCollapsed_when_dataLoaded() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertFalse(state.isPriceExpanded)
        assertFalse(state.isActionExpanded)
        assertFalse(state.isPredictionExpanded)
        assertFalse(state.isKnowledgeExpanded)
    }

    @Test
    fun should_expandPrice_when_togglePrice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isPriceExpanded)
        viewModel.togglePricePanel()
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isPriceExpanded)
    }

    @Test
    fun should_collapsePrice_when_togglePriceTwice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.togglePricePanel()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isPriceExpanded)
    }

    @Test
    fun should_expandAction_when_toggleAction() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isActionExpanded)
        viewModel.toggleActionPanel()
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isActionExpanded)
    }

    @Test
    fun should_collapseAction_when_toggleActionTwice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.toggleActionPanel()
        viewModel.toggleActionPanel()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isActionExpanded)
    }

    @Test
    fun should_expandPrediction_when_togglePrediction() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isPredictionExpanded)
        viewModel.togglePredictionPanel()
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isPredictionExpanded)
    }

    @Test
    fun should_collapsePrediction_when_togglePredictionTwice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePredictionPanel()
        viewModel.togglePredictionPanel()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isPredictionExpanded)
    }

    @Test
    fun should_expandKnowledge_when_toggleKnowledge() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isKnowledgeExpanded)
        viewModel.toggleKnowledgePanel()
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isKnowledgeExpanded)
    }

    @Test
    fun should_collapseKnowledge_when_toggleKnowledgeTwice() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.toggleKnowledgePanel()
        viewModel.toggleKnowledgePanel()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isKnowledgeExpanded)
    }

    @Test
    fun should_expandMultiple_when_multiplePanelsToggled() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        viewModel.togglePredictionPanel()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.isPriceExpanded)
        assertTrue(state.isActionExpanded)
        assertTrue(state.isPredictionExpanded)
        assertFalse(state.isKnowledgeExpanded)
    }

    @Test
    fun should_collapseAll_when_allToggledBack() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        viewModel.togglePredictionPanel()
        viewModel.toggleKnowledgePanel()
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        viewModel.togglePredictionPanel()
        viewModel.toggleKnowledgePanel()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertFalse(state.isPriceExpanded)
        assertFalse(state.isActionExpanded)
        assertFalse(state.isPredictionExpanded)
        assertFalse(state.isKnowledgeExpanded)
    }

    @Test
    fun should_maintainOtherPanels_when_oneToggled() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.toggleKnowledgePanel()
        viewModel.togglePricePanel()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertFalse(state.isPriceExpanded)
        assertFalse(state.isActionExpanded)
        assertFalse(state.isPredictionExpanded)
        assertTrue(state.isKnowledgeExpanded)
    }

    @Test
    fun should_notAffectPanels_when_inLoadingState() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        // Still loading — toggles should be no-op
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        // State is still Loading
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        // After load, all still collapsed (toggles had no effect)
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertFalse(state.isPriceExpanded)
        assertFalse(state.isActionExpanded)
    }

    @Test
    fun should_notAffectPanels_when_inErrorState() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        // Still error — no crash, state unchanged
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
    }

    @Test
    fun should_resetPanels_when_reloaded() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.toggleKnowledgePanel()
        val stateBefore = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(stateBefore.isPriceExpanded)
        assertTrue(stateBefore.isKnowledgeExpanded)
        viewModel.loadCardDetail()
        advanceUntilIdle()
        val stateAfter = viewModel.uiState.value as CardDetailUiState.Data
        // New Data instance from loadCardDetail has defaults (all false)
        assertFalse(stateAfter.isPriceExpanded)
        assertFalse(stateAfter.isKnowledgeExpanded)
    }

    @Test
    fun should_handleRapidToggles_when_rapidClicking() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        // Toggle price 5 times (odd = expanded)
        repeat(5) { viewModel.togglePricePanel() }
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isPriceExpanded)
        // Toggle price one more time (even = collapsed)
        viewModel.togglePricePanel()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isPriceExpanded)
    }

    // ===== Card content states (27-34) =====

    @Test
    fun should_showKeywords_when_dataHasKeywords() = runTest {
        val keywords = listOf(
            Keyword(text = "가격", category = KeywordCategory.PRODUCT, frequency = 3),
            Keyword(text = "계약", category = KeywordCategory.BUSINESS, frequency = 2)
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = makeCard(keywords = keywords)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(2, state.card.keywords.size)
        assertEquals("가격", state.card.keywords[0].text)
    }

    @Test
    fun should_showKeyStatements_when_present() = runTest {
        val statements = listOf(
            KeyStatement(id = 1L, speaker = "홍길동", text = "중요 발언", timestamp = "00:01:00", sentiment = Sentiment.POSITIVE, isImportant = true)
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = makeCard(keyStatements = statements)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.keyStatements.size)
        assertEquals("중요 발언", state.card.keyStatements[0].text)
    }

    @Test
    fun should_showPriceCommitments_when_present() = runTest {
        val prices = listOf(
            PriceCommitment(id = 1L, amount = 5000000.0, currency = "KRW", condition = "조건", mentionedAt = "00:05:00")
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = makeCard(priceCommitments = prices)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.priceCommitments.size)
        assertEquals(5000000.0, state.card.priceCommitments[0].amount)
    }

    @Test
    fun should_showActionItems_when_present() = runTest {
        val actions = listOf(
            ActionItem(id = 1L, description = "액션 아이템", assignee = "홍길동", dueDate = "2026-04-01", status = ActionItemStatus.OPEN)
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = makeCard(actionItems = actions)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.actionItems.size)
        assertEquals("액션 아이템", state.card.actionItems[0].description)
    }

    @Test
    fun should_showPredictedQuestions_when_present() = runTest {
        val questions = listOf(
            PredictedQuestion(id = 1L, question = "예상 질문?", suggestedAnswer = "추천 답변", relatedKnowledge = listOf("지식"), confidence = 0.9f)
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = makeCard(predictedQuestions = questions)))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.predictedQuestions.size)
        assertEquals("예상 질문?", state.card.predictedQuestions[0].question)
    }

    @Test
    fun should_showKnowledge_when_present() = runTest {
        val knowledge = listOf(
            KnowledgeArticle(id = 1L, title = "지식 아티클", content = "내용", category = "카테고리", relevanceScore = 0.7f)
        )
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(additionalKnowledge = knowledge))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.additionalKnowledge.size)
        assertEquals("지식 아티클", state.additionalKnowledge[0].title)
    }

    @Test
    fun should_handleEmptyLists_when_noContent() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.card.keywords.isEmpty())
        assertTrue(state.card.keyStatements.isEmpty())
        assertTrue(state.card.priceCommitments.isEmpty())
        assertTrue(state.card.actionItems.isEmpty())
        assertTrue(state.card.predictedQuestions.isEmpty())
        assertTrue(state.additionalKnowledge.isEmpty())
    }

    @Test
    fun should_showSentiment_when_present() = runTest {
        val card = makeCard(sentiment = Sentiment.NEGATIVE, sentimentScore = 0.3f)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(Sentiment.NEGATIVE, state.card.sentiment)
        assertEquals(0.3f, state.card.sentimentScore)
    }

    // ===== Error recovery (35-40) =====

    @Test
    fun should_recoverFromNetwork_when_retried() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("서버 연결에 실패했습니다", (viewModel.uiState.value as CardDetailUiState.Error).message)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_recoverFromTimeout_when_retried() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.TimeoutException()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("서버 응답 시간이 초과되었습니다", (viewModel.uiState.value as CardDetailUiState.Error).message)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_showNotFoundMessage_when_cardMissing() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NotFoundException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("카드를 찾을 수 없습니다", (viewModel.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showServerMessage_when_serverError() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("서버 오류가 발생했습니다", (viewModel.uiState.value as CardDetailUiState.Error).message)
    }

    @Test
    fun should_handleRapidRetries_when_multipleCalls() = runTest {
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult()),
            Result.success(makeResult()),
            Result.success(makeResult())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Error)
        viewModel.retry()
        viewModel.retry()
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_settleToFinalState_when_rapidRetries() = runTest {
        val card = makeCard(id = 99L)
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult(card = card)),
            Result.success(makeResult(card = card))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.retry()
        viewModel.retry()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(99L, state.card.id)
    }
}
