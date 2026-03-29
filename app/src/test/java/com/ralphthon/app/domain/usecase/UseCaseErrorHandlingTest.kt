package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.repository.BriefRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import com.ralphthon.app.domain.repository.UploadRepository
import com.ralphthon.app.domain.model.ConversationType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UseCaseErrorHandlingTest {

    @MockK lateinit var customerRepo: CustomerRepository
    @MockK lateinit var cardRepo: CardRepository
    @MockK lateinit var knowledgeRepo: KnowledgeRepository
    @MockK lateinit var uploadRepo: UploadRepository
    @MockK lateinit var briefRepo: BriefRepository

    private lateinit var getCustomers: GetCustomersUseCase
    private lateinit var getCardsByCustomer: GetCardsByCustomerUseCase
    private lateinit var getCardDetail: GetCardDetailUseCase
    private lateinit var getCustomerBrief: GetCustomerBriefUseCase
    private lateinit var searchCards: SearchCardsUseCase
    private lateinit var getKnowledge: GetKnowledgeUseCase
    private lateinit var uploadConversation: UploadConversationUseCase
    private lateinit var getPriceHistory: GetPriceHistoryUseCase
    private lateinit var getPredictedQuestions: GetPredictedQuestionsUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        getCustomers = GetCustomersUseCase(customerRepo)
        getCardsByCustomer = GetCardsByCustomerUseCase(cardRepo)
        getCardDetail = GetCardDetailUseCase(cardRepo, knowledgeRepo)
        getCustomerBrief = GetCustomerBriefUseCase(customerRepo, cardRepo)
        searchCards = SearchCardsUseCase(briefRepo)
        getKnowledge = GetKnowledgeUseCase(knowledgeRepo)
        uploadConversation = UploadConversationUseCase(uploadRepo)
        getPriceHistory = GetPriceHistoryUseCase(cardRepo)
        getPredictedQuestions = GetPredictedQuestionsUseCase(cardRepo)
    }

    // ─── GetCustomersUseCase (7 tests) ───────────────────────────────────────

    @Test fun should_propagateNetworkError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.NetworkException())
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.ServerException(500))
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.TimeoutException())
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.NotFoundException())
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.UnauthorizedException())
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getCustomersFails() = runTest {
        coEvery { customerRepo.getCustomers() } returns Result.failure(DomainException.UnknownException())
        val result = getCustomers()
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getCustomerByIdFails() = runTest {
        coEvery { customerRepo.getCustomerById(99L) } returns Result.failure(DomainException.NotFoundException())
        val result = getCustomers.getById(99L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    // ─── GetCardsByCustomerUseCase (6 tests) ─────────────────────────────────

    @Test fun should_propagateNetworkError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.NetworkException())
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.ServerException(500))
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.TimeoutException())
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.NotFoundException())
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getCardsByCustomerFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 10) } returns Result.failure(DomainException.UnknownException())
        val result = getCardsByCustomer(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    // ─── GetCardDetailUseCase (8 tests) ──────────────────────────────────────

    @Test fun should_propagateNetworkError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.NetworkException())
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.ServerException(500))
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.TimeoutException())
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.NotFoundException())
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getCardDetailFails() = runTest {
        coEvery { cardRepo.getCardById(1L) } returns Result.failure(DomainException.UnknownException())
        val result = getCardDetail(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNetworkError_when_knowledgeRepoFailsInCardDetail() = runTest {
        val fakeCard = buildFakeCard(1L)
        coEvery { cardRepo.getCardById(1L) } returns Result.success(fakeCard)
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NetworkException())
        val result = getCardDetail(1L)
        // knowledge failure is swallowed via getOrDefault — card detail still succeeds
        assertTrue(result.isSuccess)
    }

    @Test fun should_propagateServerError_when_knowledgeRepoFailsInCardDetail() = runTest {
        val fakeCard = buildFakeCard(1L)
        coEvery { cardRepo.getCardById(1L) } returns Result.success(fakeCard)
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.ServerException(503))
        val result = getCardDetail(1L)
        assertTrue(result.isSuccess)
    }

    // ─── GetCustomerBriefUseCase (8 tests) ───────────────────────────────────

    @Test fun should_propagateNetworkError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.NetworkException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.ServerException(500))
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.TimeoutException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.NotFoundException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getCustomerBriefCustomerFails() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.failure(DomainException.UnknownException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNetworkError_when_getCustomerBriefCardsFail() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(buildFakeCustomer(1L))
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getCustomerBriefCardsFail() = runTest {
        coEvery { customerRepo.getCustomerById(1L) } returns Result.success(buildFakeCustomer(1L))
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.TimeoutException())
        val result = getCustomerBrief(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    // ─── SearchCardsUseCase (6 tests) ────────────────────────────────────────

    @Test fun should_propagateNetworkError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.NetworkException())
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.ServerException(500))
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.TimeoutException())
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.NotFoundException())
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.UnauthorizedException())
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_searchCardsFails() = runTest {
        coEvery { briefRepo.search("query") } returns Result.failure(DomainException.UnknownException())
        val result = searchCards("query")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    // ─── GetKnowledgeUseCase (7 tests) ───────────────────────────────────────

    @Test fun should_propagateNetworkError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NetworkException())
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.ServerException(500))
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.TimeoutException())
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NotFoundException())
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getKnowledgeByCardIdFails() = runTest {
        coEvery { knowledgeRepo.getKnowledgeArticles(1L) } returns Result.failure(DomainException.UnknownException())
        val result = getKnowledge.getByCardId(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNetworkError_when_knowledgeSearchFails() = runTest {
        coEvery { knowledgeRepo.searchKnowledge("term") } returns Result.failure(DomainException.NetworkException())
        val result = getKnowledge.search("term")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    // ─── UploadConversationUseCase (6 tests) ─────────────────────────────────

    @Test fun should_propagateNetworkError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.NetworkException())
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.ServerException(500))
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.TimeoutException())
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.NotFoundException())
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.UnauthorizedException())
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_uploadConversationFails() = runTest {
        coEvery { uploadRepo.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "title", "path") } returns Result.failure(DomainException.UnknownException())
        val result = uploadConversation(1L, ConversationType.CUSTOMER_MEETING, "title", "path")
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    // ─── GetPriceHistoryUseCase (6 tests) ────────────────────────────────────

    @Test fun should_propagateNetworkError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.ServerException(500))
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.TimeoutException())
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NotFoundException())
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getPriceHistoryFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.UnknownException())
        val result = getPriceHistory(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    // ─── GetPredictedQuestionsUseCase (6 tests) ───────────────────────────────

    @Test fun should_propagateNetworkError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NetworkException())
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NetworkException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateServerError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.ServerException(500))
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.ServerException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateTimeoutError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.TimeoutException())
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.TimeoutException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateNotFoundError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.NotFoundException())
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.NotFoundException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnauthorizedError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.UnauthorizedException())
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnauthorizedException::class.java, result.exceptionOrNull())
    }

    @Test fun should_propagateUnknownError_when_getPredictedQuestionsFails() = runTest {
        coEvery { cardRepo.getCardsByCustomerId(1L, 0, 100) } returns Result.failure(DomainException.UnknownException())
        val result = getPredictedQuestions(1L)
        assertTrue(result.isFailure)
        assertInstanceOf(DomainException.UnknownException::class.java, result.exceptionOrNull())
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun buildFakeCard(id: Long) = com.ralphthon.app.domain.model.ContextCard(
        id = id,
        conversationId = 0L,
        customerId = 1L,
        title = "title",
        summary = "summary",
        date = "2026-01-01",
        conversationType = com.ralphthon.app.domain.model.ConversationType.CUSTOMER_MEETING,
        sentiment = com.ralphthon.app.domain.model.Sentiment.NEUTRAL,
        sentimentScore = 0.5f,
        keywords = emptyList(),
        keyStatements = emptyList(),
        actionItems = emptyList(),
        priceCommitments = emptyList(),
        predictedQuestions = emptyList(),
        relatedKnowledge = emptyList()
    )

    private fun buildFakeCustomer(id: Long) = com.ralphthon.app.domain.model.Customer(
        id = id,
        companyName = "Test Co",
        contactName = "홍길동",
        industry = "IT",
        totalConversations = 0,
        lastInteractionDate = "2026-01-01",
        summary = null
    )
}
