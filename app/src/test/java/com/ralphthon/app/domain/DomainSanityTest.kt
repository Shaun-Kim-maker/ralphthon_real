package com.ralphthon.app.domain

import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.BriefRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import com.ralphthon.app.domain.repository.UploadRepository
import com.ralphthon.app.domain.usecase.CardDetailResult
import com.ralphthon.app.domain.usecase.CustomerBrief
import com.ralphthon.app.domain.usecase.GetCardDetailUseCase
import com.ralphthon.app.domain.usecase.GetCardsByCustomerUseCase
import com.ralphthon.app.domain.usecase.GetCustomerBriefUseCase
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import com.ralphthon.app.domain.usecase.GetKnowledgeUseCase
import com.ralphthon.app.domain.usecase.GetPredictedQuestionsUseCase
import com.ralphthon.app.domain.usecase.GetPriceHistoryUseCase
import com.ralphthon.app.domain.usecase.SearchCardsUseCase
import com.ralphthon.app.domain.usecase.UploadConversationUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DomainSanityTest {

    @MockK
    lateinit var customerRepo: CustomerRepository

    @MockK
    lateinit var cardRepo: CardRepository

    @MockK
    lateinit var knowledgeRepo: KnowledgeRepository

    @MockK
    lateinit var uploadRepo: UploadRepository

    @MockK
    lateinit var briefRepo: BriefRepository

    private lateinit var getCustomersUseCase: GetCustomersUseCase
    private lateinit var getCardsByCustomerUseCase: GetCardsByCustomerUseCase
    private lateinit var getCardDetailUseCase: GetCardDetailUseCase
    private lateinit var getCustomerBriefUseCase: GetCustomerBriefUseCase
    private lateinit var searchCardsUseCase: SearchCardsUseCase
    private lateinit var getKnowledgeUseCase: GetKnowledgeUseCase
    private lateinit var uploadConversationUseCase: UploadConversationUseCase
    private lateinit var getPriceHistoryUseCase: GetPriceHistoryUseCase
    private lateinit var getPredictedQuestionsUseCase: GetPredictedQuestionsUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        getCustomersUseCase = GetCustomersUseCase(customerRepo)
        getCardsByCustomerUseCase = GetCardsByCustomerUseCase(cardRepo)
        getCardDetailUseCase = GetCardDetailUseCase(cardRepo, knowledgeRepo)
        getCustomerBriefUseCase = GetCustomerBriefUseCase(customerRepo, cardRepo)
        searchCardsUseCase = SearchCardsUseCase(briefRepo)
        getKnowledgeUseCase = GetKnowledgeUseCase(knowledgeRepo)
        uploadConversationUseCase = UploadConversationUseCase(uploadRepo)
        getPriceHistoryUseCase = GetPriceHistoryUseCase(cardRepo)
        getPredictedQuestionsUseCase = GetPredictedQuestionsUseCase(cardRepo)
    }

    // ===== Basic wiring (1-9) =====

    @Test
    fun should_callCustomerRepo_when_getCustomersInvoked() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.success(emptyList())

        getCustomersUseCase()

        coVerify(exactly = 1) { customerRepo.getCustomers() }
    }

    @Test
    fun should_callCardRepo_when_getCardsByCustomerInvoked() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(emptyList())

        getCardsByCustomerUseCase(1L)

        coVerify(exactly = 1) { cardRepo.getCardsByCustomerId(1L, any(), any()) }
    }

    @Test
    fun should_callBothRepos_when_getCardDetailInvoked() = runTest {
        val card = makeCard(id = 1L)
        coEvery { cardRepo.getCardById(1L) } returns Result.success(card)
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        getCardDetailUseCase(1L)

        coVerify(exactly = 1) { cardRepo.getCardById(1L) }
        coVerify(exactly = 1) { knowledgeRepo.getKnowledgeArticles(1L) }
    }

    @Test
    fun should_callBothRepos_when_getCustomerBriefInvoked() = runTest {
        val customer = makeCustomer(id = 1L)
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(customer)
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(emptyList())

        getCustomerBriefUseCase(1L)

        coVerify(exactly = 1) { customerRepo.getCustomerById(1L) }
        coVerify(exactly = 1) { cardRepo.getCardsByCustomerId(1L, any(), any()) }
    }

    @Test
    fun should_callBriefRepo_when_searchCardsInvoked() = runTest {
        coEvery { briefRepo.search("query") } returns Result.success(emptyList())

        searchCardsUseCase("query")

        coVerify(exactly = 1) { briefRepo.search("query") }
    }

    @Test
    fun should_callKnowledgeRepo_when_getKnowledgeInvoked() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        getKnowledgeUseCase.getByCardId(1L)

        coVerify(exactly = 1) { knowledgeRepo.getKnowledgeArticles(1L) }
    }

    @Test
    fun should_callUploadRepo_when_uploadInvoked() = runTest {
        val conversation = makeConversation()
        coEvery {
            uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        } returns Result.success(conversation)

        uploadConversationUseCase(1L, ConversationType.CUSTOMER_MEETING, "title", "path")

        coVerify(exactly = 1) {
            uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        }
    }

    @Test
    fun should_callCardRepo_when_getPriceHistoryInvoked() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(emptyList())

        getPriceHistoryUseCase(1L)

        coVerify(exactly = 1) { cardRepo.getCardsByCustomerId(1L, any(), any()) }
    }

    @Test
    fun should_callCardRepo_when_getPredictedQuestionsInvoked() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(emptyList())

        getPredictedQuestionsUseCase(1L)

        coVerify(exactly = 1) { cardRepo.getCardsByCustomerId(1L, any(), any()) }
    }

    // ===== ConversationType propagation (10-15) =====

    @Test
    fun should_filterCustomerMeeting_when_typeFilterApplied() = runTest {
        val cards = listOf(
            makeCard(id = 1L, conversationType = ConversationType.CUSTOMER_MEETING),
            makeCard(id = 2L, conversationType = ConversationType.INTERNAL_MEETING)
        )
        coEvery { cardRepo.getCardsByCustomerId(1L) } returns Result.success(cards)

        val result = getCardsByCustomerUseCase.getFiltered(
            customerId = 1L,
            conversationType = ConversationType.CUSTOMER_MEETING
        )

        assertTrue(result.isSuccess)
        val filtered = result.getOrNull()!!
        assertTrue(filtered.all { it.conversationType == ConversationType.CUSTOMER_MEETING })
    }

    @Test
    fun should_filterInternalMeeting_when_typeFilterApplied() = runTest {
        val cards = listOf(
            makeCard(id = 1L, conversationType = ConversationType.CUSTOMER_MEETING),
            makeCard(id = 2L, conversationType = ConversationType.INTERNAL_MEETING)
        )
        coEvery { cardRepo.getCardsByCustomerId(1L) } returns Result.success(cards)

        val result = getCardsByCustomerUseCase.getFiltered(
            customerId = 1L,
            conversationType = ConversationType.INTERNAL_MEETING
        )

        assertTrue(result.isSuccess)
        val filtered = result.getOrNull()!!
        assertTrue(filtered.all { it.conversationType == ConversationType.INTERNAL_MEETING })
    }

    @Test
    fun should_passCustomerMeeting_when_uploadCalled() = runTest {
        val conversation = makeConversation(type = ConversationType.CUSTOMER_MEETING)
        coEvery {
            uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "meeting title", "file.mp3")
        } returns Result.success(conversation)

        val result = uploadConversationUseCase(1L, ConversationType.CUSTOMER_MEETING, "meeting title", "file.mp3")

        assertTrue(result.isSuccess)
        assertEquals(ConversationType.CUSTOMER_MEETING, result.getOrNull()!!.type)
    }

    @Test
    fun should_passInternalMeeting_when_uploadCalled() = runTest {
        val conversation = makeConversation(type = ConversationType.INTERNAL_MEETING)
        coEvery {
            uploadRepo.uploadRecording(1L, ConversationType.INTERNAL_MEETING, "internal title", "file.mp3")
        } returns Result.success(conversation)

        val result = uploadConversationUseCase(1L, ConversationType.INTERNAL_MEETING, "internal title", "file.mp3")

        assertTrue(result.isSuccess)
        assertEquals(ConversationType.INTERNAL_MEETING, result.getOrNull()!!.type)
    }

    @Test
    fun should_integrateCustomerMeeting_when_briefCalled() = runTest {
        val customer = makeCustomer(id = 1L)
        val customerMeetingCard = makeCard(
            id = 1L,
            conversationType = ConversationType.CUSTOMER_MEETING,
            summary = "customer meeting summary"
        )
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(customer)
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(listOf(customerMeetingCard))

        val result = getCustomerBriefUseCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrNull()!!
        assertNotNull(brief.lastCustomerMeetingSummary)
        assertEquals("customer meeting summary", brief.lastCustomerMeetingSummary)
    }

    @Test
    fun should_integrateInternalMeeting_when_briefCalled() = runTest {
        val customer = makeCustomer(id = 1L)
        val internalMeetingCard = makeCard(
            id = 2L,
            conversationType = ConversationType.INTERNAL_MEETING,
            summary = "internal meeting summary"
        )
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(customer)
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(listOf(internalMeetingCard))

        val result = getCustomerBriefUseCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrNull()!!
        assertNotNull(brief.lastInternalMeetingSummary)
        assertEquals("internal meeting summary", brief.lastInternalMeetingSummary)
    }

    // ===== Result type validation (16-20) =====

    @Test
    fun should_returnResultSuccess_when_repoSucceeds() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val result = getCustomersUseCase()

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun should_returnResultFailure_when_repoFails() = runTest {
        val exception = RuntimeException("repo error")
        coEvery { customerRepo.getCustomers() } returns Result.failure(exception)

        val result = getCustomersUseCase()

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertEquals("repo error", result.exceptionOrNull()!!.message)
    }

    @Test
    fun should_returnListInResult_when_getCustomers() = runTest {
        val customers = listOf(makeCustomer(1L), makeCustomer(2L), makeCustomer(3L))
        coEvery { customerRepo.getCustomers() } returns Result.success(customers)

        val result: Result<List<Customer>> = getCustomersUseCase()

        assertTrue(result.isSuccess)
        val list = result.getOrNull()!!
        assertEquals(3, list.size)
        assertTrue(list[0] is Customer)
    }

    @Test
    fun should_returnCardDetailResult_when_getCardDetail() = runTest {
        val card = makeCard(id = 5L)
        val knowledgeList = listOf(makeKnowledgeArticle())
        coEvery { cardRepo.getCardById(5L) } returns Result.success(card)
        coEvery { knowledgeRepo.getKnowledgeArticles(5L) } returns Result.success(knowledgeList)

        val result: Result<CardDetailResult> = getCardDetailUseCase(5L)

        assertTrue(result.isSuccess)
        val detail = result.getOrNull()!!
        assertEquals(5L, detail.card.id)
        assertEquals(1, detail.additionalKnowledge.size)
    }

    @Test
    fun should_returnCustomerBrief_when_getBrief() = runTest {
        val customer = makeCustomer(id = 1L)
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(customer)
        coEvery { cardRepo.getCardsByCustomerId(1L, any(), any()) } returns Result.success(emptyList())

        val result: Result<CustomerBrief> = getCustomerBriefUseCase(1L)

        assertTrue(result.isSuccess)
        val brief = result.getOrNull()!!
        assertEquals(1L, brief.customer.id)
        assertNotNull(brief.lastConversationSummary)
    }

    // ===== Helpers =====

    private fun makeCustomer(
        id: Long = 1L,
        companyName: String = "Test Corp"
    ) = Customer(
        id = id,
        companyName = companyName,
        contactName = "Test Contact",
        industry = "Test Industry",
        lastInteractionDate = "2026-03-01",
        totalConversations = 0,
        summary = null
    )

    private fun makeCard(
        id: Long = 1L,
        conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
        summary: String = "test summary"
    ) = ContextCard(
        id = id,
        conversationId = id,
        customerId = 1L,
        title = "Test Card $id",
        date = "2026-03-01",
        conversationType = conversationType,
        summary = summary,
        sentiment = Sentiment.NEUTRAL,
        sentimentScore = 0.5f,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = emptyList(),
        relatedKnowledge = emptyList()
    )

    private fun makeConversation(
        id: Long = 1L,
        type: ConversationType = ConversationType.CUSTOMER_MEETING
    ) = Conversation(
        id = id,
        customerId = 1L,
        title = "Test Conversation",
        date = "2026-03-01",
        type = type,
        duration = 60,
        summary = "test summary",
        sentiment = Sentiment.NEUTRAL,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = emptyList()
    )

    private fun makeKnowledgeArticle(id: Long = 1L) = KnowledgeArticle(
        id = id,
        title = "Knowledge Article $id",
        content = "content",
        category = "General",
        relevanceScore = 0.8f
    )
}
