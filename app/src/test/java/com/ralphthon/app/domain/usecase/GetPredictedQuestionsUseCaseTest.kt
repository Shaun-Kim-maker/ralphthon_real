package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.PredictedQuestion
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetPredictedQuestionsUseCaseTest {

    @MockK
    private lateinit var repository: CardRepository
    private lateinit var useCase: GetPredictedQuestionsUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetPredictedQuestionsUseCase(repository)
    }

    private fun createQuestion(
        id: Long,
        question: String = "질문 $id",
        suggestedAnswer: String = "답변 $id",
        relatedKnowledge: List<String> = emptyList(),
        confidence: Float = 0.8f
    ) = PredictedQuestion(
        id = id,
        question = question,
        suggestedAnswer = suggestedAnswer,
        relatedKnowledge = relatedKnowledge,
        confidence = confidence
    )

    private fun createCard(
        id: Long,
        predictedQuestions: List<PredictedQuestion> = emptyList()
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
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = predictedQuestions,
        relatedKnowledge = emptyList()
    )

    // ===== Basic (1-8) =====

    @Test
    fun should_returnQuestions_when_customerHasCards() = runTest {
        val questions = listOf(createQuestion(1), createQuestion(2))
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noCards() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noPredictions() = runTest {
        val cards = listOf(createCard(1), createCard(2))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_aggregateFromMultipleCards_when_multipleCards() = runTest {
        val cards = listOf(
            createCard(1, listOf(createQuestion(1), createQuestion(2))),
            createCard(2, listOf(createQuestion(3))),
            createCard(3, listOf(createQuestion(4), createQuestion(5)))
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(5, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortByConfidenceDesc_when_returned() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.5f),
            createQuestion(2, confidence = 0.9f),
            createQuestion(3, confidence = 0.7f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val list = result.getOrNull()!!
        assertEquals(2L, list[0].id)
        assertEquals(3L, list[1].id)
        assertEquals(1L, list[2].id)
    }

    @Test
    fun should_limitTo10_when_defaultLimit() = runTest {
        val questions = (1L..15L).map { createQuestion(it, question = "질문 $it") }
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_limitToCustom_when_limitSpecified() = runTest {
        val questions = (1L..20L).map { createQuestion(it, question = "질문 $it") }
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L, limit = 5)

        assertEquals(5, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnAll_when_fewQuestions() = runTest {
        val questions = listOf(createQuestion(1), createQuestion(2), createQuestion(3))
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(3, result.getOrNull()!!.size)
    }

    // ===== Deduplication (9-13) =====

    @Test
    fun should_deduplicateQuestions_when_sameQuestionText() = runTest {
        val questions = listOf(
            createQuestion(1, question = "동일한 질문"),
            createQuestion(2, question = "동일한 질문"),
            createQuestion(3, question = "다른 질문")
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_keepFirst_when_duplicatesFound() = runTest {
        val questions = listOf(
            createQuestion(1, question = "중복 질문", confidence = 0.9f),
            createQuestion(2, question = "중복 질문", confidence = 0.5f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1L, result.getOrNull()!!.first().id)
    }

    @Test
    fun should_notDeduplicate_when_differentQuestions() = runTest {
        val questions = listOf(
            createQuestion(1, question = "질문 A"),
            createQuestion(2, question = "질문 B"),
            createQuestion(3, question = "질문 C")
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleAllDuplicates_when_allSame() = runTest {
        val questions = listOf(
            createQuestion(1, question = "같은 질문"),
            createQuestion(2, question = "같은 질문"),
            createQuestion(3, question = "같은 질문")
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_deduplicateAcrossCards_when_multipleCards() = runTest {
        val cards = listOf(
            createCard(1, listOf(createQuestion(1, question = "공통 질문"))),
            createCard(2, listOf(createQuestion(2, question = "공통 질문"))),
            createCard(3, listOf(createQuestion(3, question = "고유 질문")))
        )
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(2, result.getOrNull()!!.size)
    }

    // ===== High confidence filter (14-18) =====

    @Test
    fun should_returnHighConfidence_when_aboveThreshold() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.9f),
            createQuestion(2, confidence = 0.5f),
            createQuestion(3, confidence = 0.8f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getHighConfidence(1L, 0.7f)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.confidence >= 0.7f })
    }

    @Test
    fun should_returnEmpty_when_allLowConfidence() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.3f),
            createQuestion(2, confidence = 0.4f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getHighConfidence(1L, 0.7f)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnAll_when_allHighConfidence() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.8f),
            createQuestion(2, confidence = 0.9f),
            createQuestion(3, confidence = 0.95f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getHighConfidence(1L, 0.7f)

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_useDefaultThreshold_when_notSpecified() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.9f),
            createQuestion(2, confidence = 0.6f),
            createQuestion(3, confidence = 0.7f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getHighConfidence(1L)

        assertEquals(2, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.confidence >= 0.7f })
    }

    @Test
    fun should_useCustomThreshold_when_specified() = runTest {
        val questions = listOf(
            createQuestion(1, confidence = 0.9f),
            createQuestion(2, confidence = 0.6f),
            createQuestion(3, confidence = 0.5f)
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getHighConfidence(1L, 0.85f)

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1L, result.getOrNull()!!.first().id)
    }

    // ===== Knowledge filter (19-22) =====

    @Test
    fun should_returnWithKnowledge_when_knowledgePresent() = runTest {
        val questions = listOf(
            createQuestion(1, relatedKnowledge = listOf("지식1", "지식2")),
            createQuestion(2, relatedKnowledge = emptyList()),
            createQuestion(3, relatedKnowledge = listOf("지식3"))
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getWithKnowledge(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noKnowledge() = runTest {
        val questions = listOf(
            createQuestion(1, relatedKnowledge = emptyList()),
            createQuestion(2, relatedKnowledge = emptyList())
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getWithKnowledge(1L)

        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_excludeEmpty_when_emptyKnowledgeList() = runTest {
        val questions = listOf(
            createQuestion(1, relatedKnowledge = listOf("지식A")),
            createQuestion(2, relatedKnowledge = emptyList())
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getWithKnowledge(1L)

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1L, result.getOrNull()!!.first().id)
    }

    @Test
    fun should_includeMultiple_when_multipleHaveKnowledge() = runTest {
        val questions = listOf(
            createQuestion(1, relatedKnowledge = listOf("지식1")),
            createQuestion(2, relatedKnowledge = listOf("지식2", "지식3")),
            createQuestion(3, relatedKnowledge = listOf("지식4"))
        )
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase.getWithKnowledge(1L)

        assertEquals(3, result.getOrNull()!!.size)
    }

    // ===== Error cases (23-26) =====

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
    fun should_propagateError_when_highConfidenceFails() = runTest {
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())

        val result = useCase.getHighConfidence(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // ===== Edge cases (27-30) =====

    @Test
    fun should_preserveFields_when_returned() = runTest {
        val question = createQuestion(
            id = 42L,
            question = "테스트 질문",
            suggestedAnswer = "테스트 답변",
            relatedKnowledge = listOf("지식A", "지식B"),
            confidence = 0.95f
        )
        val cards = listOf(createCard(1, listOf(question)))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        val returned = result.getOrNull()!!.first()
        assertEquals(42L, returned.id)
        assertEquals("테스트 질문", returned.question)
        assertEquals("테스트 답변", returned.suggestedAnswer)
        assertEquals(listOf("지식A", "지식B"), returned.relatedKnowledge)
        assertEquals(0.95f, returned.confidence)
    }

    @Test
    fun should_handleSingleQuestion_when_oneOnly() = runTest {
        val questions = listOf(createQuestion(1))
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1L, result.getOrNull()!!.first().id)
    }

    @Test
    fun should_handleManyQuestions_when_100() = runTest {
        val questions = (1L..100L).map { createQuestion(it, question = "질문 $it") }
        val cards = listOf(createCard(1, questions))
        coEvery { repository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(cards)

        val result = useCase(1L)

        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_callRepository_when_invoked() = runTest {
        coEvery { repository.getCardsByCustomerId(42L, 0, 100) } returns Result.success(emptyList())

        useCase(42L)

        coVerify(exactly = 1) { repository.getCardsByCustomerId(42L, 0, 100) }
    }
}
