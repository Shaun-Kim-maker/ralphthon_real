package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.CardRepository
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
class GetCardsByCustomerUseCaseTest {

    @MockK
    private lateinit var repository: CardRepository
    private lateinit var useCase: GetCardsByCustomerUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetCardsByCustomerUseCase(repository)
    }

    private fun createCard(
        id: Long,
        customerId: Long = 1L,
        type: ConversationType = ConversationType.CUSTOMER_MEETING,
        sentiment: Sentiment = Sentiment.POSITIVE,
        date: String = "2025-03-15",
        title: String = "미팅 $id"
    ) = ContextCard(
        id = id,
        conversationId = id,
        customerId = customerId,
        title = title,
        date = date,
        conversationType = type,
        summary = "요약",
        sentiment = sentiment,
        sentimentScore = 0.8f,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = emptyList(),
        relatedKnowledge = emptyList()
    )

    // ===== Success cases (1-8) =====

    @Test
    fun should_returnCards_when_repositorySucceeds() = runTest {
        val cards = listOf(createCard(1), createCard(2), createCard(3))
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmptyList_when_noCards() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnSingleCard_when_oneExists() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(createCard(1)))

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_return10Cards_when_multipleExist() = runTest {
        val cards = (1L..10L).map { createCard(it) }
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_passCustomerId_when_invoked() = runTest {
        coEvery { repository.getCardsByCustomerId(42L, 0, 10) } returns Result.success(emptyList())

        useCase(42L)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(42L, 0, 10) }
    }

    @Test
    fun should_passPage_when_paginationRequested() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 2, 10) } returns Result.success(emptyList())

        useCase(1L, page = 2)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(1L, 2, 10) }
    }

    @Test
    fun should_passSize_when_customSize() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 20) } returns Result.success(emptyList())

        useCase(1L, size = 20)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(1L, 0, 20) }
    }

    @Test
    fun should_preserveAllFields_when_cardReturned() = runTest {
        val card = ContextCard(
            id = 99L,
            conversationId = 55L,
            customerId = 1L,
            title = "상세 미팅",
            date = "2025-03-20",
            conversationType = ConversationType.INTERNAL_MEETING,
            summary = "상세 요약",
            sentiment = Sentiment.NEGATIVE,
            sentimentScore = 0.3f,
            keywords = emptyList(),
            keyStatements = emptyList(),
            priceCommitments = emptyList(),
            actionItems = emptyList(),
            predictedQuestions = emptyList(),
            relatedKnowledge = emptyList()
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(card))

        val result = useCase(1L)
        val c = result.getOrNull()!!.first()

        assertEquals(99L, c.id)
        assertEquals(55L, c.conversationId)
        assertEquals("상세 미팅", c.title)
        assertEquals("2025-03-20", c.date)
        assertEquals(ConversationType.INTERNAL_MEETING, c.conversationType)
        assertEquals(Sentiment.NEGATIVE, c.sentiment)
        assertEquals(0.3f, c.sentimentScore)
    }

    // ===== ConversationType filter (9-15) =====

    @Test
    fun should_returnOnlyCustomerMeetings_when_typeFilterCustomer() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING),
            createCard(2, type = ConversationType.INTERNAL_MEETING),
            createCard(3, type = ConversationType.CUSTOMER_MEETING)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.conversationType == ConversationType.CUSTOMER_MEETING })
    }

    @Test
    fun should_returnOnlyInternalMeetings_when_typeFilterInternal() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING),
            createCard(2, type = ConversationType.INTERNAL_MEETING),
            createCard(3, type = ConversationType.INTERNAL_MEETING)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.INTERNAL_MEETING)

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.conversationType == ConversationType.INTERNAL_MEETING })
    }

    @Test
    fun should_returnAllCards_when_noTypeFilter() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING),
            createCard(2, type = ConversationType.INTERNAL_MEETING)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = null)

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noMatchingType() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.INTERNAL_MEETING),
            createCard(2, type = ConversationType.INTERNAL_MEETING)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_filterCorrectly_when_mixedTypes() = runTest {
        val cards = (1L..6L).map { id ->
            if (id % 2 == 0L) createCard(id, type = ConversationType.CUSTOMER_MEETING)
            else createCard(id, type = ConversationType.INTERNAL_MEETING)
        }
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_countCorrectly_when_filteredByType() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING),
            createCard(2, type = ConversationType.CUSTOMER_MEETING),
            createCard(3, type = ConversationType.CUSTOMER_MEETING),
            createCard(4, type = ConversationType.INTERNAL_MEETING)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_preserveOrder_when_typeFiltered() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, date = "2025-01-01"),
            createCard(2, type = ConversationType.INTERNAL_MEETING, date = "2025-02-01"),
            createCard(3, type = ConversationType.CUSTOMER_MEETING, date = "2025-03-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        val filtered = result.getOrNull()!!
        assertEquals(1L, filtered[0].id)
        assertEquals(3L, filtered[1].id)
    }

    // ===== Sentiment filter (16-20) =====

    @Test
    fun should_returnPositiveOnly_when_sentimentFilterPositive() = runTest {
        val cards = listOf(
            createCard(1, sentiment = Sentiment.POSITIVE),
            createCard(2, sentiment = Sentiment.NEGATIVE),
            createCard(3, sentiment = Sentiment.POSITIVE)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, sentiment = Sentiment.POSITIVE)

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.sentiment == Sentiment.POSITIVE })
    }

    @Test
    fun should_returnNegativeOnly_when_sentimentFilterNegative() = runTest {
        val cards = listOf(
            createCard(1, sentiment = Sentiment.POSITIVE),
            createCard(2, sentiment = Sentiment.NEGATIVE),
            createCard(3, sentiment = Sentiment.NEUTRAL)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, sentiment = Sentiment.NEGATIVE)

        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals(Sentiment.NEGATIVE, filtered.first().sentiment)
    }

    @Test
    fun should_returnNeutralOnly_when_sentimentFilterNeutral() = runTest {
        val cards = listOf(
            createCard(1, sentiment = Sentiment.POSITIVE),
            createCard(2, sentiment = Sentiment.NEUTRAL),
            createCard(3, sentiment = Sentiment.NEUTRAL)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, sentiment = Sentiment.NEUTRAL)

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.sentiment == Sentiment.NEUTRAL })
    }

    @Test
    fun should_returnAll_when_noSentimentFilter() = runTest {
        val cards = listOf(
            createCard(1, sentiment = Sentiment.POSITIVE),
            createCard(2, sentiment = Sentiment.NEGATIVE),
            createCard(3, sentiment = Sentiment.NEUTRAL)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, sentiment = null)

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noMatchingSentiment() = runTest {
        val cards = listOf(
            createCard(1, sentiment = Sentiment.POSITIVE),
            createCard(2, sentiment = Sentiment.POSITIVE)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, sentiment = Sentiment.NEGATIVE)

        assertEquals(0, result.getOrNull()!!.size)
    }

    // ===== Date filter (21-25) =====

    @Test
    fun should_filterByDateFrom_when_dateFromProvided() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01"),
            createCard(3, date = "2025-12-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, dateFrom = "2025-06-01")

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.date >= "2025-06-01" })
    }

    @Test
    fun should_filterByDateTo_when_dateToProvided() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01"),
            createCard(3, date = "2025-12-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, dateTo = "2025-06-01")

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.date <= "2025-06-01" })
    }

    @Test
    fun should_filterByDateRange_when_bothDatesProvided() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01"),
            createCard(3, date = "2025-09-01"),
            createCard(4, date = "2025-12-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, dateFrom = "2025-06-01", dateTo = "2025-09-01")

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.date >= "2025-06-01" && it.date <= "2025-09-01" })
    }

    @Test
    fun should_returnAll_when_noDateFilter() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L)

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_dateRangeOutOfBounds() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, dateFrom = "2026-01-01", dateTo = "2026-12-31")

        assertEquals(0, result.getOrNull()!!.size)
    }

    // ===== Sorting (26-32) =====

    @Test
    fun should_sortByDateDesc_when_defaultSort() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-01-01"),
            createCard(2, date = "2025-06-01"),
            createCard(3, date = "2025-03-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getSorted(1L)

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun should_sortByDateAsc_when_ascSort() = runTest {
        val cards = listOf(
            createCard(1, date = "2025-06-01"),
            createCard(2, date = "2025-01-01"),
            createCard(3, date = "2025-03-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getSorted(1L, GetCardsByCustomerUseCase.SortBy.DATE_ASC)

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun should_sortBySentimentDesc_when_sentimentSort() = runTest {
        val cards = listOf(
            createCard(1).copy(sentimentScore = 0.3f),
            createCard(2).copy(sentimentScore = 0.9f),
            createCard(3).copy(sentimentScore = 0.6f)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getSorted(1L, GetCardsByCustomerUseCase.SortBy.SENTIMENT_DESC)

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun should_sortByTitle_when_titleSort() = runTest {
        val cards = listOf(
            createCard(1, title = "현대 미팅"),
            createCard(2, title = "가나 미팅"),
            createCard(3, title = "다라 미팅")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getSorted(1L, GetCardsByCustomerUseCase.SortBy.TITLE)

        val sorted = result.getOrNull()!!
        assertEquals("가나 미팅", sorted[0].title)
        assertEquals("다라 미팅", sorted[1].title)
        assertEquals("현대 미팅", sorted[2].title)
    }

    @Test
    fun should_handleSingleItem_when_sorting() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(createCard(1)))

        val result = useCase.getSorted(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleEmptyList_when_sorting() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(emptyList())

        val result = useCase.getSorted(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortKoreanTitles_when_titleSort() = runTest {
        val cards = listOf(
            createCard(1, title = "하이브리드 전략 미팅"),
            createCard(2, title = "가격 협상 회의"),
            createCard(3, title = "나스닥 투자 논의")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getSorted(1L, GetCardsByCustomerUseCase.SortBy.TITLE)

        val sorted = result.getOrNull()!!
        assertEquals("가격 협상 회의", sorted[0].title)
        assertEquals("나스닥 투자 논의", sorted[1].title)
        assertEquals("하이브리드 전략 미팅", sorted[2].title)
    }

    // ===== Error cases (33-40) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase(1L)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(500, ex.code)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.getCardsByCustomerId(99L, 0, 10) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(99L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_unknownError() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.UnknownException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    @Test
    fun should_propagateError_when_filterFails() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.NetworkException())

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_propagateError_when_sortFails() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.ServerException(503))

        val result = useCase.getSorted(1L)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(503, ex.code)
    }

    // ===== Pagination (41-45) =====

    @Test
    fun should_returnFirstPage_when_page0() = runTest {
        val cards = (1L..10L).map { createCard(it) }
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase(1L, page = 0)

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnSecondPage_when_page1() = runTest {
        val cards = (11L..20L).map { createCard(it) }
        coEvery { repository.getCardsByCustomerId(1L, 1, 10) } returns Result.success(cards)

        val result = useCase(1L, page = 1)

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.size)
        assertEquals(11L, result.getOrNull()!!.first().id)
    }

    @Test
    fun should_useDefaultSize_when_noSizeSpecified() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(emptyList())

        useCase(1L)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(1L, 0, 10) }
    }

    @Test
    fun should_callRepoWithPage_when_pageProvided() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 3, 10) } returns Result.success(emptyList())

        useCase(1L, page = 3)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(1L, 3, 10) }
    }

    @Test
    fun should_callRepoWithSize_when_sizeProvided() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 5) } returns Result.success(emptyList())

        useCase(1L, size = 5)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(1L, 0, 5) }
    }

    // ===== Combined filters (46-50) =====

    @Test
    fun should_filterByTypeAndSentiment_when_bothProvided() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.POSITIVE),
            createCard(2, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.NEGATIVE),
            createCard(3, type = ConversationType.INTERNAL_MEETING, sentiment = Sentiment.POSITIVE),
            createCard(4, type = ConversationType.INTERNAL_MEETING, sentiment = Sentiment.NEGATIVE)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(
            1L,
            conversationType = ConversationType.CUSTOMER_MEETING,
            sentiment = Sentiment.POSITIVE
        )

        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered.first().id)
    }

    @Test
    fun should_filterByTypeAndDate_when_bothProvided() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, date = "2025-01-01"),
            createCard(2, type = ConversationType.CUSTOMER_MEETING, date = "2025-08-01"),
            createCard(3, type = ConversationType.INTERNAL_MEETING, date = "2025-08-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(
            1L,
            conversationType = ConversationType.CUSTOMER_MEETING,
            dateFrom = "2025-06-01"
        )

        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals(2L, filtered.first().id)
    }

    @Test
    fun should_filterByAllCriteria_when_allProvided() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.POSITIVE, date = "2025-07-01"),
            createCard(2, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.NEGATIVE, date = "2025-07-01"),
            createCard(3, type = ConversationType.INTERNAL_MEETING, sentiment = Sentiment.POSITIVE, date = "2025-07-01"),
            createCard(4, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.POSITIVE, date = "2025-01-01")
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(
            1L,
            conversationType = ConversationType.CUSTOMER_MEETING,
            sentiment = Sentiment.POSITIVE,
            dateFrom = "2025-06-01",
            dateTo = "2025-12-31"
        )

        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered.first().id)
    }

    @Test
    fun should_returnEmpty_when_conflictingFilters() = runTest {
        val cards = listOf(
            createCard(1, type = ConversationType.CUSTOMER_MEETING, sentiment = Sentiment.POSITIVE)
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(
            1L,
            conversationType = ConversationType.INTERNAL_MEETING,
            sentiment = Sentiment.NEGATIVE
        )

        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleLargeDataset_when_100Cards() = runTest {
        val cards = (1L..100L).map { id ->
            createCard(
                id = id,
                type = if (id % 2 == 0L) ConversationType.CUSTOMER_MEETING else ConversationType.INTERNAL_MEETING,
                sentiment = when (id % 3) {
                    0L -> Sentiment.POSITIVE
                    1L -> Sentiment.NEGATIVE
                    else -> Sentiment.NEUTRAL
                },
                date = "2025-0${(id % 9) + 1}-01"
            )
        }
        coEvery { repository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(cards)

        val result = useCase.getFiltered(1L, conversationType = ConversationType.CUSTOMER_MEETING)

        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.conversationType == ConversationType.CUSTOMER_MEETING })
    }
}
