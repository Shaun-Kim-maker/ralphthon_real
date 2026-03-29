package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetCustomerBriefUseCaseTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var cardRepository: CardRepository

    private lateinit var useCase: GetCustomerBriefUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetCustomerBriefUseCase(customerRepository, cardRepository)
    }

    private val testCustomer = Customer(
        id = 1L,
        companyName = "TestCorp",
        contactName = "Kim",
        industry = "IT",
        lastInteractionDate = "2025-03-20",
        totalConversations = 5,
        summary = "Good customer"
    )

    private fun createCard(
        id: Long,
        customerId: Long = 1L,
        type: ConversationType = ConversationType.CUSTOMER_MEETING,
        date: String = "2025-03-15",
        summary: String = "Summary $id",
        sentimentScore: Float = 0.5f,
        sentiment: Sentiment = Sentiment.NEUTRAL,
        priceCommitments: List<PriceCommitment> = emptyList(),
        actionItems: List<ActionItem> = emptyList(),
        predictedQuestions: List<PredictedQuestion> = emptyList()
    ) = ContextCard(
        id = id,
        conversationId = id,
        customerId = customerId,
        title = "Card $id",
        date = date,
        conversationType = type,
        summary = summary,
        sentiment = sentiment,
        sentimentScore = sentimentScore,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = priceCommitments,
        actionItems = actionItems,
        predictedQuestions = predictedQuestions,
        relatedKnowledge = emptyList()
    )

    private fun createPredictedQuestion(
        id: Long,
        question: String = "Question $id",
        confidence: Float = 0.8f
    ) = PredictedQuestion(
        id = id,
        question = question,
        suggestedAnswer = "Answer $id",
        relatedKnowledge = listOf("Knowledge $id"),
        confidence = confidence
    )

    private fun createPriceCommitment(
        id: Long,
        amount: Double = 1000.0,
        currency: String = "KRW",
        mentionedAt: String = "2025-03-15"
    ) = PriceCommitment(
        id = id,
        amount = amount,
        currency = currency,
        condition = "Condition $id",
        mentionedAt = mentionedAt
    )

    private fun createActionItem(
        id: Long,
        status: ActionItemStatus = ActionItemStatus.OPEN,
        description: String = "Action $id"
    ) = ActionItem(
        id = id,
        description = description,
        assignee = "Assignee $id",
        dueDate = "2025-04-01",
        status = status
    )

    private fun stubSuccess(cards: List<ContextCard>) {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer)
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)
    }

    // ===== Integrated summary (1-8) =====

    @Test
    fun should_returnIntegratedSummary_when_bothMeetingTypes() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "Customer talk", date = "2025-03-20"),
            createCard(2, type = ConversationType.INTERNAL_MEETING, summary = "Team sync", date = "2025-03-19")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertTrue(brief.lastConversationSummary.contains("Customer talk"))
        assertTrue(brief.lastConversationSummary.contains("Team sync"))
    }

    @Test
    fun should_returnCustomerOnly_when_noInternalMeeting() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "Customer only")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertTrue(brief.lastConversationSummary.contains("Customer only"))
        assertEquals(null, brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_returnInternalOnly_when_noCustomerMeeting() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.INTERNAL_MEETING, summary = "Internal only")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertTrue(brief.lastConversationSummary.contains("Internal only"))
        assertEquals(null, brief.lastCustomerMeetingSummary)
    }

    @Test
    fun should_returnNoRecord_when_noCards() = runTest {
        stubSuccess(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertEquals("대화 기록이 없습니다", brief.lastConversationSummary)
    }

    @Test
    fun should_useLatestMeeting_when_multipleMeetings() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "Old meeting", date = "2025-01-01"),
            createCard(2, type = ConversationType.CUSTOMER_MEETING, summary = "Latest meeting", date = "2025-03-20"),
            createCard(3, type = ConversationType.INTERNAL_MEETING, summary = "Old internal", date = "2025-01-02"),
            createCard(4, type = ConversationType.INTERNAL_MEETING, summary = "Latest internal", date = "2025-03-19")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertTrue(brief.lastConversationSummary.contains("Latest meeting"))
        assertTrue(brief.lastConversationSummary.contains("Latest internal"))
    }

    @Test
    fun should_includeCustomerTag_when_customerMeeting() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "Meeting content")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().lastConversationSummary.contains("[고객 미팅]"))
    }

    @Test
    fun should_includeInternalTag_when_internalMeeting() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.INTERNAL_MEETING, summary = "Internal content")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().lastConversationSummary.contains("[사내 회의]"))
    }

    @Test
    fun should_preserveSummaryContent_when_returned() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "Detailed discussion about pricing and timeline")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().lastConversationSummary.contains("Detailed discussion about pricing and timeline"))
    }

    // ===== Predicted questions (9-15) =====

    @Test
    fun should_returnPredictedQuestions_when_present() = runTest {
        val pq = listOf(createPredictedQuestion(1))
        val cards = listOf(createCard(1, predictedQuestions = pq))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().predictedQuestions.size)
    }

    @Test
    fun should_returnEmpty_when_noPredictedQuestions() = runTest {
        val cards = listOf(createCard(1))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().predictedQuestions.isEmpty())
    }

    @Test
    fun should_deduplicateQuestions_when_duplicates() = runTest {
        val pq1 = createPredictedQuestion(1, question = "Same question", confidence = 0.9f)
        val pq2 = createPredictedQuestion(2, question = "Same question", confidence = 0.8f)
        val cards = listOf(
            createCard(1, predictedQuestions = listOf(pq1)),
            createCard(2, predictedQuestions = listOf(pq2))
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().predictedQuestions.size)
    }

    @Test
    fun should_sortByConfidence_when_multipleQuestions() = runTest {
        val pq1 = createPredictedQuestion(1, question = "Low", confidence = 0.3f)
        val pq2 = createPredictedQuestion(2, question = "High", confidence = 0.9f)
        val pq3 = createPredictedQuestion(3, question = "Mid", confidence = 0.6f)
        val cards = listOf(createCard(1, predictedQuestions = listOf(pq1, pq2, pq3)))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val questions = result.getOrThrow().predictedQuestions
        assertEquals("High", questions[0].question)
        assertEquals("Mid", questions[1].question)
        assertEquals("Low", questions[2].question)
    }

    @Test
    fun should_limitTo5_when_manyQuestions() = runTest {
        val pqs = (1..10).map { createPredictedQuestion(it.toLong(), question = "Q$it", confidence = it / 10f) }
        val cards = listOf(createCard(1, predictedQuestions = pqs))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow().predictedQuestions.size)
    }

    @Test
    fun should_includeFromMultipleCards_when_present() = runTest {
        val pq1 = createPredictedQuestion(1, question = "Q from card 1", confidence = 0.9f)
        val pq2 = createPredictedQuestion(2, question = "Q from card 2", confidence = 0.8f)
        val cards = listOf(
            createCard(1, predictedQuestions = listOf(pq1)),
            createCard(2, predictedQuestions = listOf(pq2))
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().predictedQuestions.size)
    }

    @Test
    fun should_preserveConfidence_when_returned() = runTest {
        val pq = createPredictedQuestion(1, confidence = 0.85f)
        val cards = listOf(createCard(1, predictedQuestions = listOf(pq)))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0.85f, result.getOrThrow().predictedQuestions[0].confidence)
    }

    // ===== Price history (16-21) =====

    @Test
    fun should_returnPriceHistory_when_present() = runTest {
        val pc = listOf(createPriceCommitment(1))
        val cards = listOf(createCard(1, priceCommitments = pc))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().priceHistory.size)
    }

    @Test
    fun should_returnEmpty_when_noPriceCommitments() = runTest {
        val cards = listOf(createCard(1))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().priceHistory.isEmpty())
    }

    @Test
    fun should_sortByDate_when_multipleCommitments() = runTest {
        val pc1 = createPriceCommitment(1, mentionedAt = "2025-01-01")
        val pc2 = createPriceCommitment(2, mentionedAt = "2025-03-20")
        val pc3 = createPriceCommitment(3, mentionedAt = "2025-02-15")
        val cards = listOf(createCard(1, priceCommitments = listOf(pc1, pc2, pc3)))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val prices = result.getOrThrow().priceHistory
        assertEquals("2025-03-20", prices[0].mentionedAt)
        assertEquals("2025-02-15", prices[1].mentionedAt)
        assertEquals("2025-01-01", prices[2].mentionedAt)
    }

    @Test
    fun should_includeAmount_when_priceReturned() = runTest {
        val pc = createPriceCommitment(1, amount = 5000.0)
        val cards = listOf(createCard(1, priceCommitments = listOf(pc)))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(5000.0, result.getOrThrow().priceHistory[0].amount)
    }

    @Test
    fun should_includeCurrency_when_priceReturned() = runTest {
        val pc = createPriceCommitment(1, currency = "USD")
        val cards = listOf(createCard(1, priceCommitments = listOf(pc)))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals("USD", result.getOrThrow().priceHistory[0].currency)
    }

    @Test
    fun should_aggregateFromCards_when_multipleSources() = runTest {
        val pc1 = createPriceCommitment(1, mentionedAt = "2025-03-20")
        val pc2 = createPriceCommitment(2, mentionedAt = "2025-03-19")
        val cards = listOf(
            createCard(1, priceCommitments = listOf(pc1)),
            createCard(2, priceCommitments = listOf(pc2))
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().priceHistory.size)
    }

    // ===== Action items (22-28) =====

    @Test
    fun should_countOpenItems_when_mixedStatus() = runTest {
        val items = listOf(
            createActionItem(1, status = ActionItemStatus.OPEN),
            createActionItem(2, status = ActionItemStatus.OPEN),
            createActionItem(3, status = ActionItemStatus.OPEN),
            createActionItem(4, status = ActionItemStatus.DONE),
            createActionItem(5, status = ActionItemStatus.DONE)
        )
        val cards = listOf(createCard(1, actionItems = items))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().openActionItemsCount)
    }

    @Test
    fun should_returnZero_when_allDone() = runTest {
        val items = listOf(
            createActionItem(1, status = ActionItemStatus.DONE),
            createActionItem(2, status = ActionItemStatus.DONE)
        )
        val cards = listOf(createCard(1, actionItems = items))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().openActionItemsCount)
    }

    @Test
    fun should_returnZero_when_noActionItems() = runTest {
        val cards = listOf(createCard(1))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().openActionItemsCount)
    }

    @Test
    fun should_returnRecentOpen_when_present() = runTest {
        val items = listOf(
            createActionItem(1, status = ActionItemStatus.OPEN),
            createActionItem(2, status = ActionItemStatus.DONE),
            createActionItem(3, status = ActionItemStatus.OPEN)
        )
        val cards = listOf(createCard(1, actionItems = items))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val recent = result.getOrThrow().recentActionItems
        assertEquals(2, recent.size)
        assertTrue(recent.all { it.status == ActionItemStatus.OPEN })
    }

    @Test
    fun should_countAllOpen_when_manyCards() = runTest {
        val cards = listOf(
            createCard(1, actionItems = listOf(createActionItem(1, status = ActionItemStatus.OPEN))),
            createCard(2, actionItems = listOf(createActionItem(2, status = ActionItemStatus.OPEN))),
            createCard(3, actionItems = listOf(createActionItem(3, status = ActionItemStatus.OPEN)))
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().openActionItemsCount)
    }

    @Test
    fun should_excludeDone_when_counting() = runTest {
        val items = listOf(
            createActionItem(1, status = ActionItemStatus.OPEN),
            createActionItem(2, status = ActionItemStatus.DONE),
            createActionItem(3, status = ActionItemStatus.DONE),
            createActionItem(4, status = ActionItemStatus.DONE)
        )
        val cards = listOf(createCard(1, actionItems = items))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().openActionItemsCount)
    }

    @Test
    fun should_limitRecentTo5_when_manyOpen() = runTest {
        val items = (1..10).map { createActionItem(it.toLong(), status = ActionItemStatus.OPEN) }
        val cards = listOf(createCard(1, actionItems = items))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow().recentActionItems.size)
        assertEquals(10, result.getOrThrow().openActionItemsCount)
    }

    // ===== Sentiment (29-33) =====

    @Test
    fun should_returnPositive_when_highAvgScore() = runTest {
        val cards = listOf(
            createCard(1, sentimentScore = 0.8f),
            createCard(2, sentimentScore = 0.7f),
            createCard(3, sentimentScore = 0.9f)
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(Sentiment.POSITIVE, result.getOrThrow().overallSentiment)
    }

    @Test
    fun should_returnNegative_when_lowAvgScore() = runTest {
        val cards = listOf(
            createCard(1, sentimentScore = 0.1f),
            createCard(2, sentimentScore = 0.2f),
            createCard(3, sentimentScore = 0.3f)
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(Sentiment.NEGATIVE, result.getOrThrow().overallSentiment)
    }

    @Test
    fun should_returnNeutral_when_middleScore() = runTest {
        val cards = listOf(
            createCard(1, sentimentScore = 0.5f),
            createCard(2, sentimentScore = 0.5f)
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(Sentiment.NEUTRAL, result.getOrThrow().overallSentiment)
    }

    @Test
    fun should_returnNeutral_when_noCards() = runTest {
        stubSuccess(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(Sentiment.NEUTRAL, result.getOrThrow().overallSentiment)
    }

    @Test
    fun should_calculateFromAllCards_when_multipleCards() = runTest {
        val cards = listOf(
            createCard(1, sentimentScore = 0.9f),
            createCard(2, sentimentScore = 0.1f)
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(Sentiment.NEUTRAL, result.getOrThrow().overallSentiment)
    }

    // ===== Error cases (34-40) =====

    @Test
    fun should_returnFailure_when_customerNotFound() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_customerRepoNetworkError() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_cardRepoNetworkError() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer)
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_customerRepoTimeout() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_cardRepoTimeout() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer)
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_notCallCards_when_customerFails() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.failure(DomainException.NotFoundException())

        useCase(1L)

        coVerify(exactly = 0) { cardRepository.getCardsByCustomerId(any(), any(), any()) }
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.ServerException)
    }

    // ===== Edge + Integration (41-50) =====

    @Test
    fun should_returnBrief_when_singleCard() = runTest {
        val cards = listOf(createCard(1, summary = "Single card summary"))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
        assertEquals(1, result.getOrThrow().totalCards)
    }

    @Test
    fun should_returnBrief_when_manyCards() = runTest {
        val cards = (1..20).map { createCard(it.toLong(), date = "2025-03-%02d".format(it)) }
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(20, result.getOrThrow().totalCards)
    }

    @Test
    fun should_returnCustomerInfo_when_briefReturned() = runTest {
        stubSuccess(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertEquals(testCustomer.id, brief.customer.id)
        assertEquals(testCustomer.companyName, brief.customer.companyName)
        assertEquals(testCustomer.contactName, brief.customer.contactName)
    }

    @Test
    fun should_returnTotalCards_when_briefReturned() = runTest {
        val cards = listOf(createCard(1), createCard(2), createCard(3))
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().totalCards)
    }

    @Test
    fun should_handleMixedConversationTypes_when_mixed() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, summary = "CM1", date = "2025-03-20"),
            createCard(2, type = ConversationType.INTERNAL_MEETING, summary = "IM1", date = "2025-03-19"),
            createCard(3, type = ConversationType.CUSTOMER_MEETING, summary = "CM2", date = "2025-03-18"),
            createCard(4, type = ConversationType.INTERNAL_MEETING, summary = "IM2", date = "2025-03-17")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertEquals("CM1", brief.lastCustomerMeetingSummary)
        assertEquals("IM1", brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_handleAllCustomerMeetings_when_noInternal() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, date = "2025-03-20"),
            createCard(2, type = ConversationType.CUSTOMER_MEETING, date = "2025-03-19")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertNotNull(brief.lastCustomerMeetingSummary)
        assertEquals(null, brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_handleAllInternalMeetings_when_noCustomer() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.INTERNAL_MEETING, date = "2025-03-20"),
            createCard(2, type = ConversationType.INTERNAL_MEETING, date = "2025-03-19")
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertEquals(null, brief.lastCustomerMeetingSummary)
        assertNotNull(brief.lastInternalMeetingSummary)
    }

    @Test
    fun should_handleEmptyPredictions_when_someCardsHavePredictions() = runTest {
        val pq = createPredictedQuestion(1)
        val cards = listOf(
            createCard(1, predictedQuestions = listOf(pq)),
            createCard(2),
            createCard(3)
        )
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().predictedQuestions.size)
    }

    @Test
    fun should_handleLargeDataset_when_100Cards() = runTest {
        val cards = (1..100).map {
            createCard(
                it.toLong(),
                date = "2025-%02d-%02d".format((it % 12) + 1, (it % 28) + 1),
                sentimentScore = (it % 10) / 10f,
                predictedQuestions = if (it <= 3) listOf(createPredictedQuestion(it.toLong(), question = "Q$it")) else emptyList(),
                priceCommitments = if (it <= 2) listOf(createPriceCommitment(it.toLong())) else emptyList(),
                actionItems = if (it <= 5) listOf(createActionItem(it.toLong())) else emptyList()
            )
        }
        stubSuccess(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrThrow()
        assertEquals(100, brief.totalCards)
        assertTrue(brief.predictedQuestions.size <= 5)
        assertEquals(2, brief.priceHistory.size)
        assertEquals(5, brief.openActionItemsCount)
    }

    @Test
    fun should_callBothRepos_when_invoked() = runTest {
        stubSuccess(emptyList())

        useCase(1L)

        coVerify(exactly = 1) { customerRepository.getCustomerById(1L) }
        coVerify(exactly = 1) { cardRepository.getCardsByCustomerId(1L, 0, 100) }
    }
}
