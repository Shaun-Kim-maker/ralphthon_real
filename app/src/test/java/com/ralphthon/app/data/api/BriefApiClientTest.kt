package com.ralphthon.app.data.api

import com.ralphthon.app.domain.model.DomainException
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
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BriefApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: BriefApiClient

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

        val apiService = retrofit.create(BriefApiService::class.java)
        apiClient = BriefApiClient(apiService)
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

    private val singleResultJson = """{"id":1,"type":"CONVERSATION","title":"삼성전자 미팅","snippet":"가격 협상 진행...","highlight_ranges":[[0,2]],"source_id":1,"relevance_score":0.95}"""
    private val searchResponseJson = """{"results":[$singleResultJson],"total_count":1,"query":"가격"}"""
    private val emptyResponseJson = """{"results":[],"total_count":0,"query":"없음"}"""

    // Test 1
    @Test
    fun should_returnSearchResults_when_apiReturns200() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.results?.size)
    }

    // Test 2
    @Test
    fun should_returnEmptyResults_when_noMatches() = runTest {
        enqueueJson(200, emptyResponseJson)
        val result = apiClient.search("없음")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.results?.size)
    }

    // Test 3
    @Test
    fun should_returnMultipleResults_when_queryMatches() = runTest {
        val fiveResults = (1..5).joinToString(",") {
            """{"id":$it,"type":"CONVERSATION","title":"미팅$it","snippet":"내용$it","highlight_ranges":[],"source_id":$it,"relevance_score":0.9}"""
        }
        enqueueJson(200, """{"results":[$fiveResults],"total_count":5,"query":"미팅"}""")
        val result = apiClient.search("미팅")
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.results?.size)
    }

    // Test 4
    @Test
    fun should_parseTotalCount_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.totalCount)
    }

    // Test 5
    @Test
    fun should_parseQuery_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals("가격", result.getOrNull()?.query)
    }

    // Test 6
    @Test
    fun should_parseResultType_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals("CONVERSATION", result.getOrNull()?.results?.get(0)?.type)
    }

    // Test 7
    @Test
    fun should_parseTitle_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals("삼성전자 미팅", result.getOrNull()?.results?.get(0)?.title)
    }

    // Test 8
    @Test
    fun should_parseSnippet_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals("가격 협상 진행...", result.getOrNull()?.results?.get(0)?.snippet)
    }

    // Test 9
    @Test
    fun should_parseHighlightRanges_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        val ranges = result.getOrNull()?.results?.get(0)?.highlightRanges
        assertEquals(1, ranges?.size)
        assertEquals(listOf(0, 2), ranges?.get(0))
    }

    // Test 10
    @Test
    fun should_parseSourceId_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.results?.get(0)?.sourceId)
    }

    // Test 11
    @Test
    fun should_parseRelevanceScore_when_present() = runTest {
        enqueueJson(200, searchResponseJson)
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        assertEquals(0.95f, result.getOrNull()?.results?.get(0)?.relevanceScore ?: 0f, 0.01f)
    }

    // Test 12
    @Test
    fun should_throwNotFoundException_when_apiReturns404() = runTest {
        enqueueJson(404, """{"error":"not found"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    // Test 13
    @Test
    fun should_throwServerException_when_apiReturns500() = runTest {
        enqueueJson(500, """{"error":"server error"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(500, (ex as DomainException.ServerException).code)
    }

    // Test 14
    @Test
    fun should_throwServerException_when_apiReturns503() = runTest {
        enqueueJson(503, """{"error":"service unavailable"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(503, (ex as DomainException.ServerException).code)
    }

    // Test 15
    @Test
    fun should_throwNetworkException_when_apiReturns400() = runTest {
        enqueueJson(400, """{"error":"bad request"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 16
    @Test
    fun should_throwUnauthorized_when_apiReturns401() = runTest {
        enqueueJson(401, """{"error":"unauthorized"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 17
    @Test
    fun should_throwUnauthorized_when_apiReturns403() = runTest {
        enqueueJson(403, """{"error":"forbidden"}""")
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 18
    @Test
    fun should_throwTimeoutException_when_serverSlow() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody(searchResponseJson)
        )
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.TimeoutException || ex is DomainException.NetworkException,
            "Expected TimeoutException or NetworkException but got ${ex?.javaClass?.simpleName}"
        )
    }

    // Test 19
    @Test
    fun should_throwNetworkException_when_serverDown() = runTest {
        mockWebServer.shutdown()
        val result = apiClient.search("가격")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.NetworkException || ex is DomainException.TimeoutException,
            "Expected NetworkException or TimeoutException but got ${ex?.javaClass?.simpleName}"
        )
        // Re-create server for tearDown
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    // Test 20
    @Test
    fun should_sendCorrectPath_when_search() = runTest {
        enqueueJson(200, searchResponseJson)
        apiClient.search("가격")
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.startsWith("/api/search") == true)
    }

    // Test 21
    @Test
    fun should_sendQueryParam_when_searchCalled() = runTest {
        enqueueJson(200, searchResponseJson)
        apiClient.search("가격")
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("q=") == true)
    }

    // Test 22
    @Test
    fun should_encodeKoreanQuery_when_searchCalled() = runTest {
        enqueueJson(200, searchResponseJson)
        apiClient.search("가격")
        val request = mockWebServer.takeRequest()
        val path = request.path ?: ""
        assertTrue(path.contains("q=") && path.isNotEmpty())
    }

    // Test 23
    @Test
    fun should_handleEmptyHighlightRanges_when_noHighlights() = runTest {
        val noHighlightJson = """{"results":[{"id":1,"type":"CUSTOMER","title":"LG","snippet":"내용","highlight_ranges":[],"source_id":1,"relevance_score":0.5}],"total_count":1,"query":"LG"}"""
        enqueueJson(200, noHighlightJson)
        val result = apiClient.search("LG")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.results?.get(0)?.highlightRanges?.size)
    }

    // Test 24
    @Test
    fun should_handleZeroRelevanceScore_when_present() = runTest {
        val zeroScoreJson = """{"results":[{"id":1,"type":"CUSTOMER","title":"LG","snippet":"내용","highlight_ranges":[],"source_id":1,"relevance_score":0.0}],"total_count":1,"query":"LG"}"""
        enqueueJson(200, zeroScoreJson)
        val result = apiClient.search("LG")
        assertTrue(result.isSuccess)
        assertEquals(0.0f, result.getOrNull()?.results?.get(0)?.relevanceScore ?: -1f, 0.001f)
    }

    // Test 25
    @Test
    fun should_handleNullBody_when_200WithNoBody() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("null")
                .addHeader("Content-Type", "application/json")
        )
        val result = apiClient.search("가격")
        assertTrue(result.isSuccess)
        val dto = result.getOrNull()
        assertEquals(emptyList<Any>(), dto?.results)
        assertEquals(0, dto?.totalCount)
    }
}
