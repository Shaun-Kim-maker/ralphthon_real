package com.ralphthon.app.domain.usecase

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
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetCardDetailUseCaseTest {

    @MockK
    private lateinit var cardRepository: CardRepository

    @MockK
    private lateinit var knowledgeRepository: KnowledgeRepository

    private lateinit var useCase: GetCardDetailUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetCardDetailUseCase(cardRepository, knowledgeRepository)
    }

    private fun createCard(
        id: Long = 1L,
        conversationId: Long = 10L,
        customerId: Long = 100L,
        title: String = "테스트 카드",
        date: String = "2026-03-01",
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
        summary: String = "요약",
        sentiment: Sentiment = Sentiment.POSITIVE,
        sentimentScore: Float = 0.8f,
        keywords: List<Keyword> = emptyList(),
        keyStatements: List<KeyStatement> = emptyList(),
        priceCommitments: List<PriceCommitment> = emptyList(),
        actionItems: List<ActionItem> = emptyList(),
        predictedQuestions: List<PredictedQuestion> = emptyList(),
        relatedKnowledge: List<KnowledgeArticle> = emptyList()
    ) = ContextCard(
        id = id,
        conversationId = conversationId,
        customerId = customerId,
        title = title,
        date = date,
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

    private fun createKnowledge(id: Long = 1L) = KnowledgeArticle(
        id = id,
        title = "지식 $id",
        content = "내용",
        category = "sales",
        relevanceScore = 0.9f
    )

    // ===== Card success (1-8) =====

    @Test
    fun should_returnCardDetail_when_cardExists() = runTest {
        val card = createCard(id = 1L)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()!!.card.id)
    }

    @Test
    fun should_returnAllFields_when_fullCard() = runTest {
        val keywords = listOf(Keyword("가격", KeywordCategory.PRICE, 3))
        val keyStatements = listOf(KeyStatement(1L, "홍길동", "발언", "01:00", Sentiment.POSITIVE, true))
        val priceCommitments = listOf(PriceCommitment(1L, 1000000.0, "KRW", "조건", "01:00"))
        val actionItems = listOf(ActionItem(1L, "액션", "담당자", null, ActionItemStatus.OPEN))
        val predictedQuestions = listOf(PredictedQuestion(1L, "질문?", "답변", emptyList(), 0.9f))
        val relatedKnowledge = listOf(createKnowledge(1L))
        val card = createCard(
            keywords = keywords,
            keyStatements = keyStatements,
            priceCommitments = priceCommitments,
            actionItems = actionItems,
            predictedQuestions = predictedQuestions,
            relatedKnowledge = relatedKnowledge
        )
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val detail = result.getOrNull()!!
        assertEquals(1, detail.card.keywords.size)
        assertEquals(1, detail.card.keyStatements.size)
        assertEquals(1, detail.card.priceCommitments.size)
        assertEquals(1, detail.card.actionItems.size)
        assertEquals(1, detail.card.predictedQuestions.size)
        assertEquals(1, detail.card.relatedKnowledge.size)
    }

    @Test
    fun should_returnKeywords_when_cardHasKeywords() = runTest {
        val keywords = listOf(
            Keyword("키워드1", KeywordCategory.GENERAL, 2),
            Keyword("키워드2", KeywordCategory.PRICE, 1)
        )
        val card = createCard(keywords = keywords)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.card.keywords.size)
        assertEquals("키워드1", result.getOrNull()!!.card.keywords[0].text)
    }

    @Test
    fun should_returnKeyStatements_when_cardHasStatements() = runTest {
        val statements = listOf(
            KeyStatement(1L, "홍길동", "발언1", "00:30", Sentiment.POSITIVE, true),
            KeyStatement(2L, "김철수", "발언2", "01:00", Sentiment.NEUTRAL, false)
        )
        val card = createCard(keyStatements = statements)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.card.keyStatements.size)
    }

    @Test
    fun should_returnPriceCommitments_when_present() = runTest {
        val commitments = listOf(
            PriceCommitment(1L, 500000.0, "KRW", "기본 조건", "00:45"),
            PriceCommitment(2L, 1000000.0, "KRW", "할인 조건", "01:30")
        )
        val card = createCard(priceCommitments = commitments)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.card.priceCommitments.size)
    }

    @Test
    fun should_returnActionItems_when_present() = runTest {
        val items = listOf(
            ActionItem(1L, "후속 미팅 예약", "영업팀", "2026-04-01", ActionItemStatus.OPEN),
            ActionItem(2L, "제안서 발송", "기술팀", null, ActionItemStatus.DONE)
        )
        val card = createCard(actionItems = items)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.card.actionItems.size)
    }

    @Test
    fun should_returnPredictedQuestions_when_present() = runTest {
        val questions = listOf(
            PredictedQuestion(1L, "가격 협상 가능?", "가능합니다", emptyList(), 0.85f)
        )
        val card = createCard(predictedQuestions = questions)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(1, result.getOrNull()!!.card.predictedQuestions.size)
        assertEquals("가격 협상 가능?", result.getOrNull()!!.card.predictedQuestions[0].question)
    }

    @Test
    fun should_returnRelatedKnowledge_when_present() = runTest {
        val knowledge = listOf(createKnowledge(1L), createKnowledge(2L))
        val card = createCard(relatedKnowledge = knowledge)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.card.relatedKnowledge.size)
    }

    // ===== Knowledge integration (9-14) =====

    @Test
    fun should_returnAdditionalKnowledge_when_available() = runTest {
        val card = createCard()
        val additional = listOf(createKnowledge(10L), createKnowledge(11L))
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(additional)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.additionalKnowledge.size)
    }

    @Test
    fun should_returnEmptyKnowledge_when_noArticles() = runTest {
        val card = createCard()
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.additionalKnowledge.size)
    }

    @Test
    fun should_returnCard_when_knowledgeFails() = runTest {
        val card = createCard()
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.additionalKnowledge.size)
    }

    @Test
    fun should_mergeKnowledge_when_bothSources() = runTest {
        val cardKnowledge = listOf(createKnowledge(1L))
        val additionalKnowledge = listOf(createKnowledge(2L), createKnowledge(3L))
        val card = createCard(relatedKnowledge = cardKnowledge)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(additionalKnowledge)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.card.relatedKnowledge.size)
        assertEquals(2, result.getOrNull()!!.additionalKnowledge.size)
    }

    @Test
    fun should_returnCardOnly_when_getCardOnlyCalled() = runTest {
        val card = createCard(id = 5L)
        coEvery { cardRepository.getCardById(5L) } returns Result.success(card)

        val result = useCase.getCardOnly(5L)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull()!!.id)
    }

    @Test
    fun should_notCallKnowledge_when_getCardOnly() = runTest {
        val card = createCard()
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)

        useCase.getCardOnly(1L)

        coVerify(exactly = 0) { knowledgeRepository.getKnowledgeArticles(any()) }
    }

    // ===== Error cases (15-22) =====

    @Test
    fun should_returnFailure_when_cardNotFound() = runTest {
        coEvery { cardRepository.getCardById(999L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(999L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase(1L)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(500, ex.code)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_unknownError() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.UnknownException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    @Test
    fun should_notCallKnowledge_when_cardFails() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(DomainException.NotFoundException())

        useCase(1L)

        coVerify(exactly = 0) { knowledgeRepository.getKnowledgeArticles(any()) }
    }

    @Test
    fun should_propagateCardError_when_cardFails() = runTest {
        val exception = DomainException.ServerException(503)
        coEvery { cardRepository.getCardById(1L) } returns Result.failure(exception)

        val result = useCase(1L)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(503, ex.code)
    }

    // ===== Field verification (23-32) =====

    @Test
    fun should_preserveId_when_cardReturned() = runTest {
        val card = createCard(id = 42L)
        coEvery { cardRepository.getCardById(42L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(42L) } returns Result.success(emptyList())

        val result = useCase(42L)

        assertEquals(42L, result.getOrNull()!!.card.id)
    }

    @Test
    fun should_preserveCustomerId_when_returned() = runTest {
        val card = createCard(customerId = 77L)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(77L, result.getOrNull()!!.card.customerId)
    }

    @Test
    fun should_preserveTitle_when_returned() = runTest {
        val card = createCard(title = "중요 미팅 카드")
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals("중요 미팅 카드", result.getOrNull()!!.card.title)
    }

    @Test
    fun should_preserveDate_when_returned() = runTest {
        val card = createCard(date = "2026-03-29")
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals("2026-03-29", result.getOrNull()!!.card.date)
    }

    @Test
    fun should_preserveConversationType_when_customerMeeting() = runTest {
        val card = createCard(conversationType = ConversationType.CUSTOMER_MEETING)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(ConversationType.CUSTOMER_MEETING, result.getOrNull()!!.card.conversationType)
    }

    @Test
    fun should_preserveConversationType_when_internalMeeting() = runTest {
        val card = createCard(conversationType = ConversationType.INTERNAL_MEETING)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(ConversationType.INTERNAL_MEETING, result.getOrNull()!!.card.conversationType)
    }

    @Test
    fun should_preserveSentiment_when_positive() = runTest {
        val card = createCard(sentiment = Sentiment.POSITIVE)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(Sentiment.POSITIVE, result.getOrNull()!!.card.sentiment)
    }

    @Test
    fun should_preserveSentimentScore_when_returned() = runTest {
        val card = createCard(sentimentScore = 0.75f)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(0.75f, result.getOrNull()!!.card.sentimentScore)
    }

    @Test
    fun should_preserveSummary_when_returned() = runTest {
        val card = createCard(summary = "상세 요약 내용입니다")
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals("상세 요약 내용입니다", result.getOrNull()!!.card.summary)
    }

    @Test
    fun should_preserveConversationId_when_returned() = runTest {
        val card = createCard(conversationId = 55L)
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(55L, result.getOrNull()!!.card.conversationId)
    }

    // ===== Edge cases (33-40) =====

    @Test
    fun should_handleEmptyKeywords_when_noKeywords() = runTest {
        val card = createCard(keywords = emptyList())
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.card.keywords.size)
    }

    @Test
    fun should_handleEmptyStatements_when_noStatements() = runTest {
        val card = createCard(keyStatements = emptyList())
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.card.keyStatements.size)
    }

    @Test
    fun should_handleEmptyPriceCommitments_when_none() = runTest {
        val card = createCard(priceCommitments = emptyList())
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.card.priceCommitments.size)
    }

    @Test
    fun should_handleEmptyActionItems_when_none() = runTest {
        val card = createCard(actionItems = emptyList())
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.card.actionItems.size)
    }

    @Test
    fun should_handleEmptyPredictedQuestions_when_none() = runTest {
        val card = createCard(predictedQuestions = emptyList())
        coEvery { cardRepository.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.card.predictedQuestions.size)
    }

    @Test
    fun should_handleNegativeId_when_invalidCardId() = runTest {
        coEvery { cardRepository.getCardById(-1L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(-1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun should_handleZeroId_when_invalidCardId() = runTest {
        coEvery { cardRepository.getCardById(0L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(0L)

        assertTrue(result.isFailure)
    }

    @Test
    fun should_handleLargeId_when_maxLong() = runTest {
        val card = createCard(id = Long.MAX_VALUE)
        coEvery { cardRepository.getCardById(Long.MAX_VALUE) } returns Result.success(card)
        coEvery { knowledgeRepository.getKnowledgeArticles(Long.MAX_VALUE) } returns Result.success(emptyList())

        val result = useCase(Long.MAX_VALUE)

        assertTrue(result.isSuccess)
        assertEquals(Long.MAX_VALUE, result.getOrNull()!!.card.id)
    }
}
