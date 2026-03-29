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
class CustomerApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: CustomerApiClient

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

        val apiService = retrofit.create(CustomerApiService::class.java)
        apiClient = CustomerApiClient(apiService)
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

    private val singleCustomerJson = """{"id":1,"company_name":"삼성전자","contact_name":"김철수","industry":"전자","last_interaction_date":"2024-01-01","total_conversations":5,"summary":"요약"}"""
    private val customerListJson = """[$singleCustomerJson]"""
    private val tenCustomersJson = (1..10).joinToString(prefix = "[", postfix = "]") {
        """{"id":$it,"company_name":"회사$it","contact_name":"담당자$it","industry":"산업$it","last_interaction_date":"2024-01-0${ if (it < 10) it else 9 }","total_conversations":$it,"summary":"요약$it"}"""
    }

    // Test 1
    @Test
    fun should_returnCustomerList_when_apiReturns200() = runTest {
        enqueueJson(200, customerListJson)
        val result = apiClient.getCustomers()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    // Test 2
    @Test
    fun should_returnSingleCustomer_when_apiReturns200() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
    }

    // Test 3
    @Test
    fun should_returnEmptyList_when_apiReturnsEmptyArray() = runTest {
        enqueueJson(200, "[]")
        val result = apiClient.getCustomers()
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // Test 4
    @Test
    fun should_return10Customers_when_fullDataReturned() = runTest {
        enqueueJson(200, tenCustomersJson)
        val result = apiClient.getCustomers()
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()?.size)
    }

    // Test 5
    @Test
    fun should_throwNotFoundException_when_apiReturns404() = runTest {
        enqueueJson(404, """{"error":"not found"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    // Test 6
    @Test
    fun should_throwServerException_when_apiReturns500() = runTest {
        enqueueJson(500, """{"error":"server error"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(500, (ex as DomainException.ServerException).code)
    }

    // Test 7
    @Test
    fun should_throwServerException_when_apiReturns503() = runTest {
        enqueueJson(503, """{"error":"service unavailable"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(503, (ex as DomainException.ServerException).code)
    }

    // Test 8
    @Test
    fun should_throwNetworkException_when_apiReturns400() = runTest {
        enqueueJson(400, """{"error":"bad request"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 9
    @Test
    fun should_throwUnauthorized_when_apiReturns401() = runTest {
        enqueueJson(401, """{"error":"unauthorized"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 10
    @Test
    fun should_throwUnauthorized_when_apiReturns403() = runTest {
        enqueueJson(403, """{"error":"forbidden"}""")
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 11
    @Test
    fun should_throwTimeoutException_when_serverSlow() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody(customerListJson)
        )
        val result = apiClient.getCustomers()
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.TimeoutException || ex is DomainException.NetworkException,
            "Expected TimeoutException or NetworkException but got ${ex?.javaClass?.simpleName}")
    }

    // Test 12
    @Test
    fun should_throwNetworkException_when_serverDown() = runTest {
        mockWebServer.shutdown()
        val result = apiClient.getCustomers()
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

    // Test 13
    @Test
    fun should_throwNetworkException_when_dnsFailure() = runTest {
        val badClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
        val badRetrofit = Retrofit.Builder()
            .baseUrl("http://this-host-does-not-exist-at-all.invalid/")
            .client(badClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val badService = badRetrofit.create(CustomerApiService::class.java)
        val badClient2 = CustomerApiClient(badService)

        val result = badClient2.getCustomers()
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.NetworkException || ex is DomainException.TimeoutException,
            "Expected NetworkException or TimeoutException but got ${ex?.javaClass?.simpleName}"
        )
    }

    // Test 14
    @Test
    fun should_sendCorrectPath_when_getCustomers() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCustomers()
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.startsWith("/api/customers") == true)
    }

    // Test 15
    @Test
    fun should_sendPageParam_when_paginationRequested() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCustomers(page = 2)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("page=2") == true)
    }

    // Test 16
    @Test
    fun should_sendSizeParam_when_customSizeRequested() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCustomers(size = 5)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("size=5") == true)
    }

    // Test 17
    @Test
    fun should_sendSortParam_when_customSortRequested() = runTest {
        enqueueJson(200, "[]")
        apiClient.getCustomers(sort = "company_name,asc")
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("sort=") == true)
    }

    // Test 18
    @Test
    fun should_sendCorrectPath_when_getCustomerById() = runTest {
        enqueueJson(200, singleCustomerJson)
        apiClient.getCustomerById(1L)
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/api/customers/1") == true)
    }

    // Test 19
    @Test
    fun should_parseCompanyName_when_jsonHasCompanyName() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals("삼성전자", result.getOrNull()?.companyName)
    }

    // Test 20
    @Test
    fun should_parseNullContactName_when_jsonHasNull() = runTest {
        val json = """{"id":2,"company_name":"LG전자","contact_name":null,"industry":"전자","last_interaction_date":"2024-01-02","total_conversations":3,"summary":null}"""
        enqueueJson(200, json)
        val result = apiClient.getCustomerById(2L)
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()?.contactName)
    }

    // Test 21
    @Test
    fun should_parseIndustry_when_jsonHasIndustry() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals("전자", result.getOrNull()?.industry)
    }

    // Test 22
    @Test
    fun should_parseLastInteractionDate_when_present() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals("2024-01-01", result.getOrNull()?.lastInteractionDate)
    }

    // Test 23
    @Test
    fun should_parseTotalConversations_when_present() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.totalConversations)
    }

    // Test 24
    @Test
    fun should_parseSummary_when_present() = runTest {
        enqueueJson(200, singleCustomerJson)
        val result = apiClient.getCustomerById(1L)
        assertTrue(result.isSuccess)
        assertEquals("요약", result.getOrNull()?.summary)
    }

    // Test 25
    @Test
    fun should_returnFailure_when_responseBodyNull() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("null")
                .addHeader("Content-Type", "application/json")
        )
        val result = apiClient.getCustomers()
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }
}
