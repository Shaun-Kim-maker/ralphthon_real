package com.ralphthon.app.data.api

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.ralphthon.app.domain.model.DomainException
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: CardApiClient

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CardApiService::class.java)
        apiClient = CardApiClient(apiService)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueueJson(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(body)
                .addHeader("Content-Type", "application/json")
        )
    }

    private val singleCardJson = """{"id":1,"conversation_id":1,"customer_id":1,"title":"삼성전자 미팅","date":"2025-03-15","conversation_type":"CUSTOMER_MEETING","summary":"요약입니다","sentiment":"POSITIVE","sentiment_score":0.85,"keywords":[{"text":"로봇","category":"PRODUCT","frequency":5}],"key_statements":[{"id":1,"speaker":"김부장","text":"가격이 적절합니다","timestamp":"00:15:30","sentiment":"POSITIVE","is_important":true}],"price_commitments":[{"id":1,"amount":500000000.0,"currency":"KRW","condition":"연간 계약 시","mentioned_at":"00:20:00"}],"action_items":[{"id":1,"description":"견적서 발송","assignee":"김영업","due_date":"2025-03-20","status":"OPEN"}],"predicted_questions":[{"id":1,"question":"유지보수 비용은?","suggested_answer":"연간 10%","related_knowledge":["유지보수 가이드"],"confidence":0.9}],"related_knowledge":[{"id":1,"title":"로봇 스펙","content":"상세 내용","category":"PRODUCT","relevance_score":0.95}]}"""

    private val cardListJson = """[$singleCardJson]"""

    private fun makeCardJson(id: Long, customerId: Long = 1L): String {
        return """{"id":$id,"conversation_id":$id,"customer_id":$customerId,"title":"미팅$id","date":"2025-03-15","conversation_type":"CUSTOMER_MEETING","summary":"요약$id","sentiment":"POSITIVE","sentiment_score":0.8,"keywords":[],"key_statements":[],"price_commitments":[],"action_items":[],"predicted_questions":[],"related_knowledge":[]}"""
    }

    // Test 1
    @Test
    fun should_returnCardList_when_apiReturns200() = runTest {
        enqueueJson(200, cardListJson)
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    // Test 2
    @Test
    fun should_returnEmptyList_when_noCards() = runTest {
        enqueueJson(200, "[]")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // Test 3
    @Test
    fun should_returnSingleCard_when_getById() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
    }

    // Test 4
    @Test
    fun should_return5Cards_when_multipleReturned() = runTest {
        val fiveCards = (1..5).joinToString(prefix = "[", postfix = "]") { makeCardJson(it.toLong()) }
        enqueueJson(200, fiveCards)
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.size)
    }

    // Test 5
    @Test
    fun should_parseKeywords_when_cardHasKeywords() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.keywords?.size)
        assertEquals("로봇", card?.keywords?.get(0)?.text)
    }

    // Test 6
    @Test
    fun should_parseKeyStatements_when_cardHasStatements() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.keyStatements?.size)
        assertEquals("김부장", card?.keyStatements?.get(0)?.speaker)
    }

    // Test 7
    @Test
    fun should_parsePriceCommitments_when_cardHasCommitments() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.priceCommitments?.size)
    }

    // Test 8
    @Test
    fun should_parseActionItems_when_cardHasItems() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.actionItems?.size)
        assertEquals("견적서 발송", card?.actionItems?.get(0)?.description)
    }

    // Test 9
    @Test
    fun should_parsePredictedQuestions_when_cardHasQuestions() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.predictedQuestions?.size)
        assertEquals("유지보수 비용은?", card?.predictedQuestions?.get(0)?.question)
    }

    // Test 10
    @Test
    fun should_parseRelatedKnowledge_when_cardHasKnowledge() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(1, card?.relatedKnowledge?.size)
        assertEquals("로봇 스펙", card?.relatedKnowledge?.get(0)?.title)
    }

    // Test 11
    @Test
    fun should_throwNotFoundException_when_apiReturns404() = runTest {
        enqueueJson(404, """{"error":"not found"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    // Test 12
    @Test
    fun should_throwServerException_when_apiReturns500() = runTest {
        enqueueJson(500, """{"error":"server error"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(500, (ex as DomainException.ServerException).code)
    }

    // Test 13
    @Test
    fun should_throwServerException_when_apiReturns503() = runTest {
        enqueueJson(503, """{"error":"service unavailable"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(503, (ex as DomainException.ServerException).code)
    }

    // Test 14
    @Test
    fun should_throwNetworkException_when_apiReturns400() = runTest {
        enqueueJson(400, """{"error":"bad request"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 15
    @Test
    fun should_throwUnauthorized_when_apiReturns401() = runTest {
        enqueueJson(401, """{"error":"unauthorized"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 16
    @Test
    fun should_throwUnauthorized_when_apiReturns403() = runTest {
        enqueueJson(403, """{"error":"forbidden"}""")
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 17
    @Test
    fun should_throwTimeoutException_when_serverSlow() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody(cardListJson)
        )
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.TimeoutException || ex is DomainException.NetworkException,
            "Expected TimeoutException or NetworkException but got ${ex?.javaClass?.simpleName}"
        )
    }

    // Test 18
    @Test
    fun should_throwNetworkException_when_serverDown() = runTest {
        mockWebServer.shutdown()
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.NetworkException || ex is DomainException.TimeoutException,
            "Expected NetworkException or TimeoutException but got ${ex?.javaClass?.simpleName}"
        )
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    // Test 19
    @Test
    fun should_sendCorrectPath_when_getCardsByCustomerId() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCardsByCustomerId(1L)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.startsWith("/api/customers/1/cards") == true)
    }

    // Test 20
    @Test
    fun should_sendPageParam_when_paginationRequested() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCardsByCustomerId(1L, page = 2)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("page=2") == true)
    }

    // Test 21
    @Test
    fun should_sendSizeParam_when_customSize() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCardsByCustomerId(1L, size = 5)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("size=5") == true)
    }

    // Test 22
    @Test
    fun should_sendCorrectPath_when_getCardById() = runTest {
        enqueueJson(200, singleCardJson)
        apiClient.getCardById(5L)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/api/cards/5") == true)
    }

    // Test 23
    @Test
    fun should_parseSentiment_when_cardHasSentiment() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals("POSITIVE", result.getOrNull()?.sentiment)
    }

    // Test 24
    @Test
    fun should_parseSentimentScore_when_present() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals(0.85f, result.getOrNull()?.sentimentScore)
    }

    // Test 25
    @Test
    fun should_parseConversationType_when_present() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals("CUSTOMER_MEETING", result.getOrNull()?.conversationType)
    }

    // Test 26
    @Test
    fun should_parseTitle_when_present() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals("삼성전자 미팅", result.getOrNull()?.title)
    }

    // Test 27
    @Test
    fun should_parseSummary_when_present() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals("요약입니다", result.getOrNull()?.summary)
    }

    // Test 28
    @Test
    fun should_parseDate_when_present() = runTest {
        enqueueJson(200, singleCardJson)
        val result = apiClient.getCardById(1L)
        assertTrue(result.isSuccess)
        assertEquals("2025-03-15", result.getOrNull()?.date)
    }

    // Test 29
    @Test
    fun should_handleEmptyNestedArrays_when_noData() = runTest {
        val emptyCard = makeCardJson(99L)
        enqueueJson(200, emptyCard)
        val result = apiClient.getCardById(99L)
        assertTrue(result.isSuccess)
        val card = result.getOrNull()
        assertEquals(0, card?.keywords?.size)
        assertEquals(0, card?.keyStatements?.size)
        assertEquals(0, card?.priceCommitments?.size)
        assertEquals(0, card?.actionItems?.size)
        assertEquals(0, card?.predictedQuestions?.size)
        assertEquals(0, card?.relatedKnowledge?.size)
    }

    // Test 30
    @Test
    fun should_handleLargeResponse_when_manyCards() = runTest {
        val fiftyCards = (1..50).joinToString(prefix = "[", postfix = "]") { makeCardJson(it.toLong()) }
        enqueueJson(200, fiftyCards)
        val result = apiClient.getCardsByCustomerId(1L)
        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull()?.size)
    }
}
