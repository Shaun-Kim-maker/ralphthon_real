package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.CardRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetPriceHistoryUseCaseTest {

    @MockK
    private lateinit var repository: CardRepository
    private lateinit var useCase: GetPriceHistoryUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetPriceHistoryUseCase(repository)
    }

    private fun createPrice(
        id: Long,
        amount: Double = 1000.0,
        currency: String = "KRW",
        condition: String = "기본 조건",
        mentionedAt: String = "2025-03-15"
    ) = PriceCommitment(id = id, amount = amount, currency = currency, condition = condition, mentionedAt = mentionedAt)

    private fun createCard(
        id: Long,
        priceCommitments: List<PriceCommitment> = emptyList()
    ) = ContextCard(
        id = id,
        conversationId = id,
        customerId = 1L,
        title = "미팅 $id",
        date = "2025-03-15",
        conversationType = ConversationType.CUSTOMER_MEETING,
        summary = "요약",
        sentiment = Sentiment.POSITIVE,
        sentimentScore = 0.8f,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = priceCommitments,
        actionItems = emptyList(),
        predictedQuestions = emptyList(),
        relatedKnowledge = emptyList()
    )

    // ===== Basic (1-8) =====

    @Test
    fun should_returnPriceHistory_when_customerHasCommitments() = runTest {
        val prices = listOf(createPrice(1), createPrice(2))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.allCommitments.size)
    }

    @Test
    fun should_returnEmpty_when_noCommitments() = runTest {
        val cards = listOf(createCard(1))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.allCommitments.size)
    }

    @Test
    fun should_aggregateFromMultipleCards_when_multipleCards() = runTest {
        val cards = listOf(
            createCard(1, listOf(createPrice(1), createPrice(2))),
            createCard(2, listOf(createPrice(3))),
            createCard(3, listOf(createPrice(4), createPrice(5)))
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(5, result.getOrNull()!!.allCommitments.size)
    }

    @Test
    fun should_sortByDateDesc_when_returned() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-01-01"),
            createPrice(2, mentionedAt = "2025-06-01"),
            createPrice(3, mentionedAt = "2025-03-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val commitments = result.getOrNull()!!.allCommitments
        assertEquals(2L, commitments[0].id)
        assertEquals(3L, commitments[1].id)
        assertEquals(1L, commitments[2].id)
    }

    @Test
    fun should_returnLatestPrice_when_present() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-01-01"),
            createPrice(2, mentionedAt = "2025-06-01"),
            createPrice(3, mentionedAt = "2025-03-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(2L, result.getOrNull()!!.latestPrice!!.id)
    }

    @Test
    fun should_returnNullLatest_when_empty() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertNull(result.getOrNull()!!.latestPrice)
    }

    @Test
    fun should_returnTotalCount_when_present() = runTest {
        val prices = listOf(createPrice(1), createPrice(2), createPrice(3))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(3, result.getOrNull()!!.totalCount)
    }

    @Test
    fun should_returnZeroCount_when_empty() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertEquals(0, result.getOrNull()!!.totalCount)
    }

    // ===== Currency grouping (9-14) =====

    @Test
    fun should_groupByCurrency_when_multipleCurrencies() = runTest {
        val prices = listOf(
            createPrice(1, currency = "KRW"),
            createPrice(2, currency = "USD"),
            createPrice(3, currency = "KRW")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val byCurrency = result.getOrNull()!!.byCurrency
        assertEquals(2, byCurrency["KRW"]!!.size)
        assertEquals(1, byCurrency["USD"]!!.size)
    }

    @Test
    fun should_returnSingleGroup_when_oneCurrency() = runTest {
        val prices = listOf(createPrice(1, currency = "KRW"), createPrice(2, currency = "KRW"))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val byCurrency = result.getOrNull()!!.byCurrency
        assertEquals(1, byCurrency.size)
        assertEquals(2, byCurrency["KRW"]!!.size)
    }

    @Test
    fun should_returnEmptyMap_when_noCommitments() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.getOrNull()!!.byCurrency.isEmpty())
    }

    @Test
    fun should_preserveAmounts_when_grouped() = runTest {
        val prices = listOf(
            createPrice(1, amount = 50000.0, currency = "KRW"),
            createPrice(2, amount = 100.0, currency = "USD")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val byCurrency = result.getOrNull()!!.byCurrency
        assertEquals(50000.0, byCurrency["KRW"]!!.first().amount)
        assertEquals(100.0, byCurrency["USD"]!!.first().amount)
    }

    @Test
    fun should_getByCurrency_when_currencySpecified() = runTest {
        val prices = listOf(
            createPrice(1, currency = "KRW"),
            createPrice(2, currency = "USD"),
            createPrice(3, currency = "KRW")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByCurrency(1L, "KRW")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.currency == "KRW" })
    }

    @Test
    fun should_returnEmpty_when_currencyNotFound() = runTest {
        val prices = listOf(createPrice(1, currency = "KRW"))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByCurrency(1L, "USD")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    // ===== Date range (15-19) =====

    @Test
    fun should_filterByDateRange_when_bothDates() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-01-01"),
            createPrice(2, mentionedAt = "2025-06-01"),
            createPrice(3, mentionedAt = "2025-09-01"),
            createPrice(4, mentionedAt = "2025-12-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByDateRange(1L, "2025-06-01", "2025-09-01")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnAll_when_wideRange() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-01-01"),
            createPrice(2, mentionedAt = "2025-06-01"),
            createPrice(3, mentionedAt = "2025-12-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByDateRange(1L, "2020-01-01", "2030-12-31")

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_narrowRange() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-01-01"),
            createPrice(2, mentionedAt = "2025-12-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByDateRange(1L, "2025-06-01", "2025-06-30")

        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_includeEndpoints_when_exactDates() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-06-01"),
            createPrice(2, mentionedAt = "2025-09-01")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByDateRange(1L, "2025-06-01", "2025-09-01")

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleSameFromTo_when_singleDay() = runTest {
        val prices = listOf(
            createPrice(1, mentionedAt = "2025-06-15"),
            createPrice(2, mentionedAt = "2025-06-16")
        )
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getByDateRange(1L, "2025-06-15", "2025-06-15")

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1L, result.getOrNull()!!.first().id)
    }

    // ===== Error cases (20-25) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase(1L)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(500, ex.code)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.getCardsByCustomerId(99L, 0, 100) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(99L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_propagateError_when_dateRangeFails() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())

        val result = useCase.getByDateRange(1L, "2025-01-01", "2025-12-31")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // ===== Edge cases (26-30) =====

    @Test
    fun should_handleSingleCommitment_when_oneOnly() = runTest {
        val prices = listOf(createPrice(1, amount = 999.0, currency = "USD"))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.totalCount)
        assertEquals(1L, result.getOrNull()!!.latestPrice!!.id)
    }

    @Test
    fun should_handleLargeAmounts_when_billionKRW() = runTest {
        val prices = listOf(createPrice(1, amount = 1_000_000_000.0, currency = "KRW"))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(1_000_000_000.0, result.getOrNull()!!.latestPrice!!.amount)
    }

    @Test
    fun should_preserveCondition_when_returned() = runTest {
        val prices = listOf(createPrice(1, condition = "연간 계약 조건부"))
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals("연간 계약 조건부", result.getOrNull()!!.allCommitments.first().condition)
    }

    @Test
    fun should_handleManyCommitments_when_50plus() = runTest {
        val prices = (1L..55L).map { createPrice(it) }
        val cards = listOf(createCard(1, prices))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(55, result.getOrNull()!!.totalCount)
    }

    @Test
    fun should_callRepository_when_invoked() = runTest {
        coEvery { repository.getCardsByCustomerId(42L, 0, 100) } returns Result.success(emptyList())

        useCase(42L)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(42L, 0, 100) }
    }
}
