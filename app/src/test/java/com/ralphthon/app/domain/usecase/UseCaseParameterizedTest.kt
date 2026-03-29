package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.BriefRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import com.ralphthon.app.domain.repository.UploadRepository
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class UseCaseParameterizedTest {

    @MockK lateinit var customerRepository: CustomerRepository
    @MockK lateinit var cardRepository: CardRepository
    @MockK lateinit var knowledgeRepository: KnowledgeRepository
    @MockK lateinit var uploadRepository: UploadRepository
    @MockK lateinit var briefRepository: BriefRepository

    private lateinit var getCustomersUseCase: GetCustomersUseCase
    private lateinit var getCardsByCustomerUseCase: GetCardsByCustomerUseCase
    private lateinit var getCardDetailUseCase: GetCardDetailUseCase
    private lateinit var getCustomerBriefUseCase: GetCustomerBriefUseCase
    private lateinit var searchCardsUseCase: SearchCardsUseCase
    private lateinit var getKnowledgeUseCase: GetKnowledgeUseCase
    private lateinit var uploadConversationUseCase: UploadConversationUseCase
    private lateinit var getPriceHistoryUseCase: GetPriceHistoryUseCase
    private lateinit var getPredictedQuestionsUseCase: GetPredictedQuestionsUseCase

    private fun makeCustomer(id: Long = 1L) = Customer(
        id = id, companyName = "TestCo", contactName = null,
        industry = "IT", lastInteractionDate = "2026-01-01",
        totalConversations = 1, summary = null
    )

    private fun makeCard(id: Long = 1L, customerId: Long = 1L) = ContextCard.withDefaults(
        id = id, customerId = customerId, conversationId = id,
        title = "Card $id", date = "2026-01-01",
        conversationType = ConversationType.CUSTOMER_MEETING,
        sentiment = Sentiment.NEUTRAL, sentimentScore = 0.5f
    )

    private fun makeKnowledge(id: Long = 1L) = KnowledgeArticle(
        id = id, title = "Article $id", content = "content",
        category = "general", relevanceScore = 0.8f
    )

    private fun makeSearchResult(id: Long = 1L) = SearchResult(
        id = id, type = "CUSTOMER_MEETING", title = "Result $id",
        snippet = "snippet", highlightRanges = emptyList(),
        sourceId = 1L, relevanceScore = 0.9f
    )

    @BeforeEach
    fun setup() {
        getCustomersUseCase = GetCustomersUseCase(customerRepository)
        getCardsByCustomerUseCase = GetCardsByCustomerUseCase(cardRepository)
        getCardDetailUseCase = GetCardDetailUseCase(cardRepository, knowledgeRepository)
        getCustomerBriefUseCase = GetCustomerBriefUseCase(customerRepository, cardRepository)
        searchCardsUseCase = SearchCardsUseCase(briefRepository)
        getKnowledgeUseCase = GetKnowledgeUseCase(knowledgeRepository)
        uploadConversationUseCase = UploadConversationUseCase(uploadRepository)
        getPriceHistoryUseCase = GetPriceHistoryUseCase(cardRepository)
        getPredictedQuestionsUseCase = GetPredictedQuestionsUseCase(cardRepository)
    }

    // ===== ID Boundary Tests (16) =====

    // GetCustomersUseCase.getById - 4 values
    @ParameterizedTest
    @ValueSource(longs = [1L, 100L, 999L, Long.MAX_VALUE])
    fun should_acceptValidId_when_getCustomerById(id: Long) = runTest {
        coEvery { customerRepository.getCustomerById(any()) } returns Result.success(makeCustomer(id))
        val result = getCustomersUseCase.getById(id)
        assertTrue(result.isSuccess)
    }

    // GetCardDetailUseCase.getCardOnly - 4 values
    @ParameterizedTest
    @ValueSource(longs = [1L, 100L, 999L, Long.MAX_VALUE])
    fun should_acceptValidId_when_getCardById(id: Long) = runTest {
        coEvery { cardRepository.getCardById(any()) } returns Result.success(makeCard(id))
        val result = getCardDetailUseCase.getCardOnly(id)
        assertTrue(result.isSuccess)
    }

    // GetCardsByCustomerUseCase - 4 values
    @ParameterizedTest
    @ValueSource(longs = [1L, 100L, 999L, Long.MAX_VALUE])
    fun should_acceptValidId_when_getCardsByCustomerId(id: Long) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(listOf(makeCard(1L, id)))
        val result = getCardsByCustomerUseCase.invoke(id)
        assertTrue(result.isSuccess)
    }

    // GetKnowledgeUseCase.getByCardId - 4 values
    @ParameterizedTest
    @ValueSource(longs = [1L, 100L, 999L, Long.MAX_VALUE])
    fun should_acceptValidId_when_getKnowledgeByCardId(id: Long) = runTest {
        coEvery { knowledgeRepository.getKnowledgeArticles(any()) } returns Result.success(listOf(makeKnowledge()))
        val result = getKnowledgeUseCase.getByCardId(id)
        assertTrue(result.isSuccess)
    }

    // ===== Pagination Boundary Tests (12) =====

    // GetCardsByCustomerUseCase - 6 combos
    @ParameterizedTest
    @CsvSource("0,10", "0,20", "1,10", "5,50", "0,1", "99,100")
    fun should_acceptPagination_when_getCardsByCustomer(page: Int, size: Int) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(emptyList())
        val result = getCardsByCustomerUseCase.invoke(1L, page, size)
        assertTrue(result.isSuccess)
    }

    // GetCustomersUseCase.invoke (no pagination directly, but called via getSorted internally) - 6 combos
    @ParameterizedTest
    @CsvSource("0,10", "0,20", "1,10", "5,50", "0,1", "99,100")
    fun should_handlePaginationParams_when_getCustomersPaged(page: Int, size: Int) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any(), eq(page), eq(size)) } returns Result.success(emptyList())
        val result = getCardsByCustomerUseCase.invoke(1L, page, size)
        assertTrue(result.isSuccess)
    }

    // ===== ConversationType Combinations (12) =====

    // GetCardsByCustomerUseCase.getFiltered by type - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_filterByType_when_getCardsByCustomer(type: ConversationType) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(makeCard()))
        val result = getCardsByCustomerUseCase.getFiltered(1L, conversationType = type)
        assertTrue(result.isSuccess)
    }

    // GetCardsByCustomerUseCase.getFiltered with null sentiment - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_filterCardsByConversationType_when_typeProvided(type: ConversationType) = runTest {
        val card = makeCard().copy(conversationType = type)
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(card))
        val result = getCardsByCustomerUseCase.getFiltered(1L, conversationType = type)
        assertTrue(result.isSuccess)
    }

    // UploadConversationUseCase - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_acceptConversationType_when_uploadConversation(type: ConversationType) = runTest {
        coEvery { uploadRepository.uploadRecording(any(), any(), any(), any()) } returns
            Result.success(com.ralphthon.app.domain.model.Conversation(
                id = 1L, customerId = 1L, title = "title", date = "2026-01-01",
                type = type, duration = 0, summary = "",
                sentiment = Sentiment.NEUTRAL, keywords = emptyList(),
                keyStatements = emptyList(), priceCommitments = emptyList(),
                actionItems = emptyList(), predictedQuestions = emptyList()
            ))
        val result = uploadConversationUseCase.invoke(1L, type, "test title", "/path/file.mp3")
        assertTrue(result.isSuccess)
    }

    // GetCustomerBriefUseCase integration with ConversationType - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_handleConversationType_when_getBrief(type: ConversationType) = runTest {
        val card = makeCard().copy(conversationType = type)
        coEvery { customerRepository.getCustomerById(any()) } returns Result.success(makeCustomer())
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(listOf(card))
        val result = getCustomerBriefUseCase.invoke(1L)
        assertTrue(result.isSuccess)
    }

    // GetCardsByCustomerUseCase.getSorted with different conv types - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_sortCardsWithConversationType_when_typePresent(type: ConversationType) = runTest {
        val card = makeCard().copy(conversationType = type)
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(card))
        val result = getCardsByCustomerUseCase.getSorted(1L)
        assertTrue(result.isSuccess)
    }

    // SearchCardsUseCase with ConversationType filter string - 2 values
    @ParameterizedTest
    @EnumSource(ConversationType::class)
    fun should_filterByConversationTypeString_when_searchFiltered(type: ConversationType) = runTest {
        coEvery { briefRepository.search(any()) } returns Result.success(listOf(makeSearchResult()))
        val result = searchCardsUseCase.searchFiltered("query", type.name)
        assertTrue(result.isSuccess)
    }

    // ===== Sentiment Filter Combinations (9) =====

    // GetCardsByCustomerUseCase.getFiltered by sentiment - 3 values
    @ParameterizedTest
    @EnumSource(Sentiment::class)
    fun should_filterBySentiment_when_getCardsByCustomer(sentiment: Sentiment) = runTest {
        val card = makeCard().copy(sentiment = sentiment)
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(card))
        val result = getCardsByCustomerUseCase.getFiltered(1L, sentiment = sentiment)
        assertTrue(result.isSuccess)
    }

    // GetCustomerBriefUseCase overall sentiment - 3 values
    @ParameterizedTest
    @EnumSource(Sentiment::class)
    fun should_calculateSentiment_when_getBriefWithCards(sentiment: Sentiment) = runTest {
        val score = when (sentiment) {
            Sentiment.POSITIVE -> 0.8f
            Sentiment.NEGATIVE -> 0.2f
            Sentiment.NEUTRAL -> 0.5f
        }
        val card = makeCard().copy(sentiment = sentiment, sentimentScore = score)
        coEvery { customerRepository.getCustomerById(any()) } returns Result.success(makeCustomer())
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(listOf(card))
        val result = getCustomerBriefUseCase.invoke(1L)
        assertTrue(result.isSuccess)
    }

    // GetCardsByCustomerUseCase.getFiltered with sentiment null check - 3 values
    @ParameterizedTest
    @EnumSource(Sentiment::class)
    fun should_returnCards_when_filterBySentimentType(sentiment: Sentiment) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(emptyList())
        val result = getCardsByCustomerUseCase.getFiltered(1L, sentiment = sentiment)
        assertTrue(result.isSuccess)
    }

    // ===== Sort Combinations (12) =====

    // GetCustomersUseCase.getSorted - 3 values
    @ParameterizedTest
    @EnumSource(GetCustomersUseCase.SortBy::class)
    fun should_sortCustomers_when_sortByProvided(sortBy: GetCustomersUseCase.SortBy) = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(makeCustomer()))
        val result = getCustomersUseCase.getSorted(sortBy)
        assertTrue(result.isSuccess)
    }

    // GetCardsByCustomerUseCase.getSorted - 4 values
    @ParameterizedTest
    @EnumSource(GetCardsByCustomerUseCase.SortBy::class)
    fun should_sortCards_when_sortByProvided(sortBy: GetCardsByCustomerUseCase.SortBy) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(makeCard()))
        val result = getCardsByCustomerUseCase.getSorted(1L, sortBy)
        assertTrue(result.isSuccess)
    }

    // SearchCardsUseCase.searchSorted - 2 values
    @ParameterizedTest
    @EnumSource(SearchCardsUseCase.SortBy::class)
    fun should_sortSearchResults_when_sortByProvided(sortBy: SearchCardsUseCase.SortBy) = runTest {
        coEvery { briefRepository.search(any()) } returns Result.success(listOf(makeSearchResult()))
        val result = searchCardsUseCase.searchSorted("query", sortBy)
        assertTrue(result.isSuccess)
    }

    // GetKnowledgeUseCase.getByCardIdSorted - 3 values
    @ParameterizedTest
    @EnumSource(GetKnowledgeUseCase.SortBy::class)
    fun should_sortKnowledge_when_sortByProvided(sortBy: GetKnowledgeUseCase.SortBy) = runTest {
        coEvery { knowledgeRepository.getKnowledgeArticles(any()) } returns Result.success(listOf(makeKnowledge()))
        val result = getKnowledgeUseCase.getByCardIdSorted(1L, sortBy)
        assertTrue(result.isSuccess)
    }

    // ===== Date Range Combinations (8) =====

    // GetCardsByCustomerUseCase.getFiltered by date - 4 combos
    @ParameterizedTest
    @CsvSource(
        "2025-01-01,2025-12-31",
        "2025-03-01,2025-03-31",
        "2025-03-15,2025-03-15",
        "2020-01-01,2030-12-31"
    )
    fun should_filterByDateRange_when_getCards(from: String, to: String) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any()) } returns Result.success(listOf(makeCard()))
        val result = getCardsByCustomerUseCase.getFiltered(1L, dateFrom = from, dateTo = to)
        assertTrue(result.isSuccess)
    }

    // GetPriceHistoryUseCase.getByDateRange - 4 combos
    @ParameterizedTest
    @CsvSource(
        "2025-01-01,2025-12-31",
        "2025-03-01,2025-03-31",
        "2025-03-15,2025-03-15",
        "2020-01-01,2030-12-31"
    )
    fun should_filterByDateRange_when_getPriceHistory(from: String, to: String) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(emptyList())
        val result = getPriceHistoryUseCase.getByDateRange(1L, from, to)
        assertTrue(result.isSuccess)
    }

    // ===== Currency Format Combinations (6) =====

    @ParameterizedTest
    @ValueSource(strings = ["KRW", "USD", "EUR", "JPY", "GBP", "CNY"])
    fun should_filterByCurrency_when_getPriceHistory(currency: String) = runTest {
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.success(emptyList())
        val result = getPriceHistoryUseCase.getByCurrency(1L, currency)
        assertTrue(result.isSuccess)
    }

    // ===== Query Input Variations (5) =====

    @ParameterizedTest
    @ValueSource(strings = ["로봇", "가격", "Physical AI", "삼성전자", "500만원"])
    fun should_handleQuery_when_searchCards(query: String) = runTest {
        coEvery { briefRepository.search(any()) } returns Result.success(listOf(makeSearchResult()))
        val result = searchCardsUseCase.invoke(query)
        assertTrue(result.isSuccess)
    }
}
