package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.BriefRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import com.ralphthon.app.domain.repository.UploadRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UseCaseConcurrencyTest {

    @MockK lateinit var customerRepository: CustomerRepository
    @MockK lateinit var cardRepository: CardRepository
    @MockK lateinit var knowledgeRepository: KnowledgeRepository
    @MockK lateinit var briefRepository: BriefRepository
    @MockK lateinit var uploadRepository: UploadRepository

    private lateinit var getCustomers: GetCustomersUseCase
    private lateinit var getCards: GetCardsByCustomerUseCase
    private lateinit var searchCards: SearchCardsUseCase
    private lateinit var getCustomerBrief: GetCustomerBriefUseCase
    private lateinit var uploadConversation: UploadConversationUseCase
    private lateinit var getKnowledge: GetKnowledgeUseCase
    private lateinit var getPriceHistory: GetPriceHistoryUseCase
    private lateinit var getPredictedQuestions: GetPredictedQuestionsUseCase
    private lateinit var getCardDetail: GetCardDetailUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        getCustomers = GetCustomersUseCase(customerRepository)
        getCards = GetCardsByCustomerUseCase(cardRepository)
        searchCards = SearchCardsUseCase(briefRepository)
        getCustomerBrief = GetCustomerBriefUseCase(customerRepository, cardRepository)
        uploadConversation = UploadConversationUseCase(uploadRepository)
        getKnowledge = GetKnowledgeUseCase(knowledgeRepository)
        getPriceHistory = GetPriceHistoryUseCase(cardRepository)
        getPredictedQuestions = GetPredictedQuestionsUseCase(cardRepository)
        getCardDetail = GetCardDetailUseCase(cardRepository, knowledgeRepository)
    }

    // ===== Test data helpers =====

    private fun testCustomer(id: Long = 1L) = Customer(
        id = id, companyName = "삼성전자", contactName = "홍길동",
        industry = "전자", lastInteractionDate = "2026-03-01",
        totalConversations = 5, summary = "요약"
    )

    private fun testCard(id: Long = 1L, customerId: Long = 1L) = ContextCard(
        id = id, conversationId = id, customerId = customerId,
        title = "카드$id", date = "2026-03-01",
        conversationType = ConversationType.CUSTOMER_MEETING,
        summary = "요약", sentiment = Sentiment.NEUTRAL, sentimentScore = 0.5f,
        keywords = emptyList(), keyStatements = emptyList(),
        priceCommitments = emptyList(), actionItems = emptyList(),
        predictedQuestions = emptyList(), relatedKnowledge = emptyList()
    )

    private fun testSearchResult(id: Long = 1L) = SearchResult(
        id = id, type = "card", title = "결과$id", snippet = "내용",
        highlightRanges = emptyList(), sourceId = id, relevanceScore = 0.9f
    )

    private fun testKnowledge(id: Long = 1L) = KnowledgeArticle(
        id = id, title = "지식$id", content = "내용", category = "제품", relevanceScore = 0.8f
    )

    private fun testSearchResults() = listOf(testSearchResult(1), testSearchResult(2))

    // ===== Parallel execution (1-10) =====

    @Test
    fun should_handleParallelCustomerCalls_when_concurrent() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(50)
            Result.success(listOf(testCustomer()))
        }
        val d1 = async { getCustomers() }
        val d2 = async { getCustomers() }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleParallelCardCalls_when_concurrent() = runTest {
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } coAnswers {
            delay(50)
            Result.success(listOf(testCard()))
        }
        val d1 = async { getCards(1L) }
        val d2 = async { getCards(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleParallelSearchCalls_when_concurrent() = runTest {
        coEvery { briefRepository.search("query") } coAnswers {
            delay(50)
            Result.success(testSearchResults())
        }
        val d1 = async { searchCards("query") }
        val d2 = async { searchCards("query") }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleMixedUseCases_when_concurrent() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer()))
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(testCard()))
        val d1 = async { getCustomers() }
        val d2 = async { getCards(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_returnIndependentResults_when_parallelExecution() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer(1L)))
        coEvery { cardRepository.getCardsByCustomerId(2L, 0, 10) } returns Result.success(listOf(testCard(id = 2L, customerId = 2L)))
        val d1 = async { getCustomers() }
        val d2 = async { getCards(2L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertEquals(1L, r1.getOrNull()!!.first().id)
        assertEquals(2L, r2.getOrNull()!!.first().id)
    }

    @Test
    fun should_handleParallelBriefCalls_when_concurrent() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer(1L))
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        val d1 = async { getCustomerBrief(1L) }
        val d2 = async { getCustomerBrief(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleParallelKnowledgeCalls_when_concurrent() = runTest {
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } coAnswers {
            delay(30)
            Result.success(listOf(testKnowledge()))
        }
        val d1 = async { getKnowledge.getByCardId(1L) }
        val d2 = async { getKnowledge.getByCardId(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleParallelPriceHistory_when_concurrent() = runTest {
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        val d1 = async { getPriceHistory(1L) }
        val d2 = async { getPriceHistory(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleParallelPredictedQuestions_when_concurrent() = runTest {
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        val d1 = async { getPredictedQuestions(1L) }
        val d2 = async { getPredictedQuestions(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handle5ParallelCalls_when_highConcurrency() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer()))
        val results = (1..5).map {
            async { getCustomers() }
        }.awaitAll()
        assertEquals(5, results.size)
        assertTrue(results.all { it.isSuccess })
    }

    // ===== Cancellation (11-18) =====

    @Test
    fun should_cancelGracefully_when_customerJobCancelled() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(1000)
            Result.success(listOf(testCustomer()))
        }
        val job = launch { getCustomers() }
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_cancelGracefully_when_cardJobCancelled() = runTest {
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } coAnswers {
            delay(1000)
            Result.success(listOf(testCard()))
        }
        val job = launch { getCards(1L) }
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_cancelGracefully_when_searchJobCancelled() = runTest {
        coEvery { briefRepository.search("query") } coAnswers {
            delay(1000)
            Result.success(testSearchResults())
        }
        val job = launch { searchCards("query") }
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_cancelGracefully_when_briefJobCancelled() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } coAnswers {
            delay(1000)
            Result.success(testCustomer())
        }
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        val job = launch { getCustomerBrief(1L) }
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_cancelGracefully_when_uploadJobCancelled() = runTest {
        coEvery { uploadRepository.uploadRecording(any(), any(), any(), any()) } coAnswers {
            delay(1000)
            Result.success(
                com.ralphthon.app.domain.model.Conversation(
                    id = 1L, customerId = 1L, title = "t", date = "2026-03-01",
                    type = ConversationType.CUSTOMER_MEETING, duration = 60,
                    summary = "s", sentiment = Sentiment.NEUTRAL,
                    keywords = emptyList(), keyStatements = emptyList(),
                    priceCommitments = emptyList(), actionItems = emptyList(),
                    predictedQuestions = emptyList()
                )
            )
        }
        val job = launch { uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "제목", "/path/file.m4a") }
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_notAffectOther_when_oneJobCancelled() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(500)
            Result.success(listOf(testCustomer()))
        }
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(testCard()))
        val cancelJob = launch { getCustomers() }
        val normalDeferred = async { getCards(1L) }
        cancelJob.cancel()
        val result = normalDeferred.await()
        assertTrue(result.isSuccess)
    }

    @Test
    fun should_throwCancellation_when_jobCancelledDuringExecution() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(1000)
            Result.success(listOf(testCustomer()))
        }
        var caught = false
        val job = launch {
            try {
                getCustomers()
            } catch (e: CancellationException) {
                caught = true
                throw e
            }
        }
        delay(10)
        job.cancel()
        job.join()
        assertTrue(caught)
    }

    @Test
    fun should_cancelChildCoroutines_when_parentCancelled() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(500)
            Result.success(listOf(testCustomer()))
        }
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } coAnswers {
            delay(500)
            Result.success(listOf(testCard()))
        }
        val completedJobs = mutableListOf<String>()
        val parentJob = launch {
            supervisorScope {
                val child1 = launch {
                    try {
                        getCustomers()
                        completedJobs.add("customers")
                    } catch (e: CancellationException) {
                        throw e
                    }
                }
                val child2 = launch {
                    try {
                        getCards(1L)
                        completedJobs.add("cards")
                    } catch (e: CancellationException) {
                        throw e
                    }
                }
                delay(20)
                child1.cancel()
                child2.cancel()
            }
        }
        parentJob.join()
        assertTrue(completedJobs.isEmpty())
    }

    // ===== Sequential calls (19-26) =====

    @Test
    fun should_handleRapidCalls_when_calledSequentially() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer()))
        val results = (1..10).map { getCustomers() }
        assertEquals(10, results.size)
        assertTrue(results.all { it.isSuccess })
    }

    @Test
    fun should_returnCorrectResult_when_calledAfterPrevious() = runTest {
        coEvery { customerRepository.getCustomers() } returnsMany listOf(
            Result.success(listOf(testCustomer(1L))),
            Result.success(listOf(testCustomer(2L)))
        )
        val r1 = getCustomers()
        val r2 = getCustomers()
        assertEquals(1L, r1.getOrNull()!!.first().id)
        assertEquals(2L, r2.getOrNull()!!.first().id)
    }

    @Test
    fun should_notCache_when_calledTwiceSequentially() = runTest {
        var callCount = 0
        coEvery { customerRepository.getCustomers() } coAnswers {
            callCount++
            Result.success(listOf(testCustomer()))
        }
        getCustomers()
        getCustomers()
        assertEquals(2, callCount)
    }

    @Test
    fun should_handleAlternatingUseCases_when_interleaved() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer()))
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(testCard()))
        val r1 = getCustomers()
        val r2 = getCards(1L)
        val r3 = getCustomers()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertTrue(r3.isSuccess)
    }

    @Test
    fun should_handleSequentialBriefs_when_differentCustomers() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer(1L))
        coEvery { customerRepository.getCustomerById(2L) } returns Result.success(testCustomer(2L))
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        coEvery { cardRepository.getCardsByCustomerId(2L, 0, 100) } returns Result.success(emptyList())
        val r1 = getCustomerBrief(1L)
        val r2 = getCustomerBrief(2L)
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertEquals(1L, r1.getOrNull()!!.customer.id)
        assertEquals(2L, r2.getOrNull()!!.customer.id)
    }

    @Test
    fun should_handleSequentialSearches_when_differentQueries() = runTest {
        coEvery { briefRepository.search("a") } returns Result.success(listOf(testSearchResult(1L)))
        coEvery { briefRepository.search("b") } returns Result.success(listOf(testSearchResult(2L)))
        val r1 = searchCards("a")
        val r2 = searchCards("b")
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertEquals(1L, r1.getOrNull()!!.first().id)
        assertEquals(2L, r2.getOrNull()!!.first().id)
    }

    @Test
    fun should_handleSequentialUploads_when_multipleCalls() = runTest {
        val conv1 = com.ralphthon.app.domain.model.Conversation(
            1L, 1L, "제목1", "2026-03-01", ConversationType.CUSTOMER_MEETING,
            60, "s", Sentiment.NEUTRAL, emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
        )
        val conv2 = com.ralphthon.app.domain.model.Conversation(
            2L, 1L, "제목2", "2026-03-01", ConversationType.INTERNAL_MEETING,
            60, "s", Sentiment.NEUTRAL, emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
        )
        coEvery { uploadRepository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목1", "/f1.m4a") } returns Result.success(conv1)
        coEvery { uploadRepository.uploadRecording(1L, ConversationType.INTERNAL_MEETING, "제목2", "/f2.m4a") } returns Result.success(conv2)
        val r1 = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "제목1", "/f1.m4a")
        val r2 = uploadConversation(1L, ConversationType.INTERNAL_MEETING, "제목2", "/f2.m4a")
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertEquals(1L, r1.getOrNull()!!.id)
        assertEquals(2L, r2.getOrNull()!!.id)
    }

    @Test
    fun should_handleSequentialPriceHistory_when_different() = runTest {
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        coEvery { cardRepository.getCardsByCustomerId(2L, 0, 100) } returns Result.success(emptyList())
        val r1 = getPriceHistory(1L)
        val r2 = getPriceHistory(2L)
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    // ===== Delayed responses (27-33) =====

    @Test
    fun should_completeNormally_when_repoDelays100ms() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(100)
            Result.success(listOf(testCustomer()))
        }
        val result = getCustomers()
        assertTrue(result.isSuccess)
    }

    @Test
    fun should_completeNormally_when_repoDelays500ms() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(500)
            Result.success(listOf(testCustomer()))
        }
        val result = getCustomers()
        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleMixedDelays_when_parallelWithDifferentSpeeds() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(500)
            Result.success(listOf(testCustomer()))
        }
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } coAnswers {
            delay(10)
            Result.success(listOf(testCard()))
        }
        val slowDeferred = async { getCustomers() }
        val fastDeferred = async { getCards(1L) }
        val fast = fastDeferred.await()
        val slow = slowDeferred.await()
        assertTrue(fast.isSuccess)
        assertTrue(slow.isSuccess)
    }

    @Test
    fun should_returnFirst_when_racingTwoSearches() = runTest {
        coEvery { briefRepository.search("query") } coAnswers {
            delay(100)
            Result.success(testSearchResults())
        }
        val d1 = async { searchCards("query") }
        val d2 = async { searchCards("query") }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_handleDelayedCustomerRepo_when_briefCalled() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } coAnswers {
            delay(200)
            Result.success(testCustomer())
        }
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } returns Result.success(emptyList())
        val result = getCustomerBrief(1L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleDelayedCardRepo_when_briefCalled() = runTest {
        coEvery { customerRepository.getCustomerById(1L) } returns Result.success(testCustomer())
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 100) } coAnswers {
            delay(300)
            Result.success(emptyList())
        }
        val result = getCustomerBrief(1L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleDelayedKnowledge_when_cardDetailCalled() = runTest {
        coEvery { cardRepository.getCardById(1L) } returns Result.success(testCard())
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } coAnswers {
            delay(200)
            Result.success(listOf(testKnowledge()))
        }
        val result = getCardDetail(1L)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.additionalKnowledge.size)
    }

    // ===== Error + concurrency (34-40) =====

    @Test
    fun should_failGracefully_when_oneParallelCallFails() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.failure(DomainException.NetworkException())
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.success(listOf(testCard()))
        val d1 = async { getCustomers() }
        val d2 = async { getCards(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isFailure)
        assertTrue(r2.isSuccess)
    }

    @Test
    fun should_propagateError_when_allParallelCallsFail() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.failure(DomainException.NetworkException())
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.ServerException(500))
        val d1 = async { getCustomers() }
        val d2 = async { getCards(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        assertTrue(r1.isFailure)
        assertTrue(r2.isFailure)
    }

    @Test
    fun should_handleMixedResults_when_someFailSomeSucceed() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.success(listOf(testCustomer()))
        coEvery { cardRepository.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.NotFoundException())
        coEvery { briefRepository.search("q") } returns Result.success(testSearchResults())
        val d1 = async { getCustomers() }
        val d2 = async { getCards(1L) }
        val d3 = async { searchCards("q") }
        val r1 = d1.await()
        val r2 = d2.await()
        val r3 = d3.await()
        assertTrue(r1.isSuccess)
        assertTrue(r2.isFailure)
        assertTrue(r3.isSuccess)
    }

    @Test
    fun should_retainError_when_cancelledAndFailed() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(1000)
            Result.failure(DomainException.NetworkException())
        }
        val job = launch {
            try {
                getCustomers()
            } catch (e: CancellationException) {
                throw e
            }
        }
        delay(10)
        job.cancel()
        job.join()
        assertFalse(job.isActive)
    }

    @Test
    fun should_handleTimeout_when_concurrentWithTimeout() = runTest {
        coEvery { customerRepository.getCustomers() } coAnswers {
            delay(2000)
            Result.success(listOf(testCustomer()))
        }
        var timedOut = false
        try {
            withTimeout(100) {
                getCustomers()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            timedOut = true
        }
        assertTrue(timedOut)
    }

    @Test
    fun should_handleNetworkError_when_concurrentCalls() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.failure(DomainException.NetworkException())
        coEvery { cardRepository.getCardsByCustomerId(any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val d1 = async { getCustomers() }
        val d2 = async { getCards(1L) }
        val results = listOf(d1.await(), d2.await())
        assertTrue(results.all { it.isFailure })
        assertTrue(results.all { it.exceptionOrNull() is DomainException.NetworkException })
    }

    @Test
    fun should_isolateErrors_when_differentUseCasesFail() = runTest {
        coEvery { customerRepository.getCustomers() } returns Result.failure(DomainException.ServerException(500))
        coEvery { briefRepository.search("q") } returns Result.success(testSearchResults())
        coEvery { knowledgeRepository.getKnowledgeArticles(1L) } returns Result.success(listOf(testKnowledge()))
        val d1 = async { getCustomers() }
        val d2 = async { searchCards("q") }
        val d3 = async { getKnowledge.getByCardId(1L) }
        val r1 = d1.await()
        val r2 = d2.await()
        val r3 = d3.await()
        assertTrue(r1.isFailure)
        assertTrue(r2.isSuccess)
        assertTrue(r3.isSuccess)
    }
}
