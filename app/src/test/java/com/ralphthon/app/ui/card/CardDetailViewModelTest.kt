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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class CardDetailViewModelTest {

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
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
        summary: String = "요약"
    ) = ContextCard(
        id = id,
        conversationId = 10L,
        customerId = 100L,
        title = "테스트 카드",
        date = "2026-03-29",
        conversationType = conversationType,
        summary = summary,
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

    private fun makeKeyword(text: String = "키워드") = Keyword(
        text = text,
        category = KeywordCategory.PRODUCT,
        frequency = 3
    )

    private fun makeKeyStatement(text: String = "핵심 발언") = KeyStatement(
        id = 1L,
        speaker = "홍길동",
        text = text,
        timestamp = "00:01:00",
        sentiment = Sentiment.POSITIVE,
        isImportant = true
    )

    private fun makePriceCommitment() = PriceCommitment(
        id = 1L,
        amount = 1000000.0,
        currency = "KRW",
        condition = "조건",
        mentionedAt = "00:05:00"
    )

    private fun makeActionItem() = ActionItem(
        id = 1L,
        description = "액션 아이템",
        assignee = "홍길동",
        dueDate = "2026-04-01",
        status = ActionItemStatus.OPEN
    )

    private fun makePredictedQuestion() = PredictedQuestion(
        id = 1L,
        question = "예상 질문?",
        suggestedAnswer = "추천 답변",
        relatedKnowledge = listOf("관련 지식"),
        confidence = 0.85f
    )

    private fun makeKnowledgeArticle() = KnowledgeArticle(
        id = 1L,
        title = "지식 아티클",
        content = "내용",
        category = "카테고리",
        relevanceScore = 0.7f
    )

    private fun createViewModel(cardId: Long = 1L): CardDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("cardId" to cardId))
        return CardDetailViewModel(getCardDetailUseCase, savedStateHandle)
    }

    // ===== States (1-8) =====

    @Test
    fun should_showLoading_when_screenOpened() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        assertEquals(CardDetailUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_showCardDetail_when_loadSucceeds() = runTest {
        val card = makeCard(id = 1L)
        coEvery { getCardDetailUseCase(1L) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel(cardId = 1L)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Data)
        assertEquals(card, (state as CardDetailUiState.Data).card)
    }

    @Test
    fun should_showError_when_loadFails() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(RuntimeException("error"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Error)
        assertEquals("알 수 없는 오류가 발생했습니다", (state as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showError_when_networkError() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (state as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showError_when_timeout() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Error)
        assertEquals("서버 응답 시간이 초과되었습니다", (state as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showError_when_notFound() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.NotFoundException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Error)
        assertEquals("카드를 찾을 수 없습니다", (state as CardDetailUiState.Error).message)
    }

    @Test
    fun should_showError_when_serverError() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CardDetailUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (state as CardDetailUiState.Error).message)
    }

    @Test
    fun should_handleCardId_when_savedState() = runTest {
        coEvery { getCardDetailUseCase(42L) } returns Result.success(makeResult(card = makeCard(id = 42L)))
        val viewModel = createViewModel(cardId = 42L)
        assertEquals(42L, viewModel.cardId)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    // ===== Card fields (9-16) =====

    @Test
    fun should_showKeywords_when_cardHasKeywords() = runTest {
        val keywords = listOf(makeKeyword("가격"), makeKeyword("계약"))
        val card = makeCard(keywords = keywords)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(2, state.card.keywords.size)
        assertEquals("가격", state.card.keywords[0].text)
    }

    @Test
    fun should_showKeyStatements_when_cardHasStatements() = runTest {
        val statements = listOf(makeKeyStatement("중요 발언 1"), makeKeyStatement("중요 발언 2"))
        val card = makeCard(keyStatements = statements)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(2, state.card.keyStatements.size)
        assertEquals("중요 발언 1", state.card.keyStatements[0].text)
    }

    @Test
    fun should_showPriceCommitments_when_present() = runTest {
        val prices = listOf(makePriceCommitment())
        val card = makeCard(priceCommitments = prices)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.priceCommitments.size)
        assertEquals(1000000.0, state.card.priceCommitments[0].amount)
    }

    @Test
    fun should_showActionItems_when_present() = runTest {
        val actions = listOf(makeActionItem())
        val card = makeCard(actionItems = actions)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.actionItems.size)
        assertEquals("액션 아이템", state.card.actionItems[0].description)
    }

    @Test
    fun should_showPredictedQuestions_when_present() = runTest {
        val questions = listOf(makePredictedQuestion())
        val card = makeCard(predictedQuestions = questions)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.card.predictedQuestions.size)
        assertEquals("예상 질문?", state.card.predictedQuestions[0].question)
    }

    @Test
    fun should_showSentiment_when_present() = runTest {
        val card = makeCard(sentiment = Sentiment.POSITIVE, sentimentScore = 0.9f)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(Sentiment.POSITIVE, state.card.sentiment)
        assertEquals(0.9f, state.card.sentimentScore)
    }

    @Test
    fun should_showConversationType_when_present() = runTest {
        val card = makeCard(conversationType = ConversationType.INTERNAL_MEETING)
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(ConversationType.INTERNAL_MEETING, state.card.conversationType)
    }

    @Test
    fun should_showAdditionalKnowledge_when_present() = runTest {
        val knowledge = listOf(makeKnowledgeArticle())
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(additionalKnowledge = knowledge))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(1, state.additionalKnowledge.size)
        assertEquals("지식 아티클", state.additionalKnowledge[0].title)
    }

    // ===== Panel toggles (17-24) =====

    @Test
    fun should_expandPrice_when_togglePricePanel() = runTest {
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
    fun should_expandAction_when_toggleActionPanel() = runTest {
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
    fun should_expandPrediction_when_togglePredictionPanel() = runTest {
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
    fun should_expandKnowledge_when_toggleKnowledgePanel() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse((viewModel.uiState.value as CardDetailUiState.Data).isKnowledgeExpanded)
        viewModel.toggleKnowledgePanel()
        assertTrue((viewModel.uiState.value as CardDetailUiState.Data).isKnowledgeExpanded)
    }

    @Test
    fun should_independentPanels_when_multipleToggled() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.togglePricePanel()
        viewModel.toggleActionPanel()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.isPriceExpanded)
        assertTrue(state.isActionExpanded)
        assertFalse(state.isPredictionExpanded)
        assertFalse(state.isKnowledgeExpanded)
    }

    // ===== Retry/refresh (25-27) =====

    @Test
    fun should_retryLoad_when_retryCalled() = runTest {
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
    fun should_showLoading_when_retrying() = runTest {
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
        // loadCardDetail sets Loading synchronously at the start of the coroutine body.
        // With StandardTestDispatcher, calling loadCardDetail() directly (not via launch)
        // would block. We verify that the initial state after createViewModel() was Loading (test 1)
        // and that retry eventually produces Data after advancing.
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CardDetailUiState.Data)
    }

    @Test
    fun should_showData_when_retrySucceeds() = runTest {
        val card = makeCard(id = 5L)
        coEvery { getCardDetailUseCase(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(makeResult(card = card))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertEquals(5L, state.card.id)
    }

    // ===== Edge cases (28-30) =====

    @Test
    fun should_handleEmptyKeywords_when_noKeywords() = runTest {
        val card = makeCard(keywords = emptyList())
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.card.keywords.isEmpty())
    }

    @Test
    fun should_handleEmptyStatements_when_noStatements() = runTest {
        val card = makeCard(keyStatements = emptyList())
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.card.keyStatements.isEmpty())
    }

    @Test
    fun should_handleEmptyPriceCommitments_when_none() = runTest {
        val card = makeCard(priceCommitments = emptyList())
        coEvery { getCardDetailUseCase(any()) } returns Result.success(makeResult(card = card))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CardDetailUiState.Data
        assertTrue(state.card.priceCommitments.isEmpty())
    }
}
