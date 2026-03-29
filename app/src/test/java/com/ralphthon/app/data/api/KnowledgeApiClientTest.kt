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
class KnowledgeApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: KnowledgeApiClient

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

        val apiService = retrofit.create(KnowledgeApiService::class.java)
        apiClient = KnowledgeApiClient(apiService)
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

    private val singleArticleJson = """{"id":1,"title":"Physical AI 스펙","content":"로봇 제품 사양...","category":"PRODUCT","relevance_score":0.95}"""
    private val articleListJson = """[$singleArticleJson]"""

    // Test 1
    @Test
    fun should_returnArticles_when_apiReturns200() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    // Test 2
    @Test
    fun should_returnEmptyList_when_noArticles() = runTest {
        enqueueJson(200, "[]")
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // Test 3
    @Test
    fun should_return3Articles_when_multipleReturned() = runTest {
        val threeArticles = (1..3).joinToString(prefix = "[", postfix = "]") {
            """{"id":$it,"title":"Article$it","content":"Content$it","category":"PRODUCT","relevance_score":0.9}"""
        }
        enqueueJson(200, threeArticles)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    // Test 4
    @Test
    fun should_parseTitle_when_present() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals("Physical AI 스펙", result.getOrNull()?.first()?.title)
    }

    // Test 5
    @Test
    fun should_parseContent_when_present() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals("로봇 제품 사양...", result.getOrNull()?.first()?.content)
    }

    // Test 6
    @Test
    fun should_parseCategory_when_present() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals("PRODUCT", result.getOrNull()?.first()?.category)
    }

    // Test 7
    @Test
    fun should_parseRelevanceScore_when_present() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals(0.95f, result.getOrNull()?.first()?.relevanceScore)
    }

    // Test 8
    @Test
    fun should_returnSearchResults_when_searchSucceeds() = runTest {
        enqueueJson(200, articleListJson)
        val result = apiClient.searchKnowledge("로봇")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    // Test 9
    @Test
    fun should_returnEmptySearch_when_noMatches() = runTest {
        enqueueJson(200, "[]")
        val result = apiClient.searchKnowledge("없는검색어")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // Test 10
    @Test
    fun should_throwNotFoundException_when_404() = runTest {
        enqueueJson(404, """{"error":"not found"}""")
        val result = apiClient.getKnowledgeArticles(999L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    // Test 11
    @Test
    fun should_throwServerException_when_500() = runTest {
        enqueueJson(500, """{"error":"server error"}""")
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(500, (ex as DomainException.ServerException).code)
    }

    // Test 12
    @Test
    fun should_throwServerException_when_503() = runTest {
        enqueueJson(503, """{"error":"service unavailable"}""")
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(503, (ex as DomainException.ServerException).code)
    }

    // Test 13
    @Test
    fun should_throwNetworkException_when_400() = runTest {
        enqueueJson(400, """{"error":"bad request"}""")
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 14
    @Test
    fun should_throwUnauthorized_when_401() = runTest {
        enqueueJson(401, """{"error":"unauthorized"}""")
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 15
    @Test
    fun should_throwTimeoutException_when_slow() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody(articleListJson)
        )
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.TimeoutException || ex is DomainException.NetworkException,
            "Expected TimeoutException or NetworkException but got ${ex?.javaClass?.simpleName}"
        )
    }

    // Test 16
    @Test
    fun should_throwNetworkException_when_down() = runTest {
        mockWebServer.shutdown()
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.NetworkException || ex is DomainException.TimeoutException,
            "Expected NetworkException or TimeoutException but got ${ex?.javaClass?.simpleName}"
        )
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    // Test 17
    @Test
    fun should_sendCorrectPath_when_getArticles() = runTest {
        enqueueJson(200, "[]")
        apiClient.getKnowledgeArticles(1L)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/api/cards/1/knowledge") == true)
    }

    // Test 18
    @Test
    fun should_sendCorrectPath_when_search() = runTest {
        enqueueJson(200, "[]")
        apiClient.searchKnowledge("test")
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/api/knowledge/search") == true)
    }

    // Test 19
    @Test
    fun should_sendQueryParam_when_search() = runTest {
        enqueueJson(200, "[]")
        apiClient.searchKnowledge("로봇")
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("q=") == true)
    }

    // Test 20
    @Test
    fun should_handleNullBody_when_200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("null")
                .addHeader("Content-Type", "application/json")
        )
        val result = apiClient.getKnowledgeArticles(1L)
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }
}
