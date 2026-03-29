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
import java.io.File
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UploadApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: UploadApiClient
    private lateinit var tempFile: File

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

        val apiService = retrofit.create(UploadApiService::class.java)
        apiClient = UploadApiClient(apiService)

        tempFile = File.createTempFile("test_recording", ".m4a")
        tempFile.writeText("fake audio content")
        tempFile.deleteOnExit()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        tempFile.delete()
    }

    private fun enqueueJson(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(body)
                .addHeader("Content-Type", "application/json")
        )
    }

    private val successJson = """{"conversation_id":1,"status":"COMPLETED","message":"업로드 완료"}"""

    // Test 1
    @Test
    fun should_returnResponse_when_uploadSucceeds() = runTest {
        enqueueJson(200, successJson)
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isSuccess)
    }

    // Test 2
    @Test
    fun should_parseConversationId_when_present() = runTest {
        enqueueJson(200, successJson)
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.conversationId)
    }

    // Test 3
    @Test
    fun should_parseStatus_when_present() = runTest {
        enqueueJson(200, successJson)
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isSuccess)
        assertEquals("COMPLETED", result.getOrNull()?.status)
    }

    // Test 4
    @Test
    fun should_parseMessage_when_present() = runTest {
        enqueueJson(200, successJson)
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isSuccess)
        assertEquals("업로드 완료", result.getOrNull()?.message)
    }

    // Test 5
    @Test
    fun should_sendMultipart_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        val request = mockWebServer.takeRequest()
        val contentType = request.getHeader("Content-Type") ?: ""
        assertTrue(contentType.contains("multipart/form-data"), "Expected multipart but got: $contentType")
    }

    // Test 6
    @Test
    fun should_sendCustomerId_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(42L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        val request = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("42"), "Expected customer_id 42 in body")
    }

    // Test 7
    @Test
    fun should_sendConversationType_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "INTERNAL_MEETING", "내부 회의", tempFile)
        val request = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("INTERNAL_MEETING"), "Expected conversation_type in body")
    }

    // Test 8
    @Test
    fun should_sendTitle_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "특별한 회의 제목", tempFile)
        val request = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("특별한 회의 제목"), "Expected title in body")
    }

    // Test 9
    @Test
    fun should_sendFile_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        val request = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("fake audio content"), "Expected file content in body")
    }

    // Test 10
    @Test
    fun should_sendCorrectPath_when_upload() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        val request = mockWebServer.takeRequest()
        assertEquals("/api/upload", request.path)
    }

    // Test 11
    @Test
    fun should_throwNotFoundException_when_404() = runTest {
        enqueueJson(404, """{"error":"not found"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    // Test 12
    @Test
    fun should_throwServerException_when_500() = runTest {
        enqueueJson(500, """{"error":"server error"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(500, (ex as DomainException.ServerException).code)
    }

    // Test 13
    @Test
    fun should_throwServerException_when_503() = runTest {
        enqueueJson(503, """{"error":"service unavailable"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is DomainException.ServerException)
        assertEquals(503, (ex as DomainException.ServerException).code)
    }

    // Test 14
    @Test
    fun should_throwNetworkException_when_400() = runTest {
        enqueueJson(400, """{"error":"bad request"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 15
    @Test
    fun should_throwUnauthorized_when_401() = runTest {
        enqueueJson(401, """{"error":"unauthorized"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    // Test 16
    @Test
    fun should_throwNetworkException_when_413() = runTest {
        enqueueJson(413, """{"error":"file too large"}""")
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    // Test 17
    @Test
    fun should_throwTimeoutException_when_slow() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody(successJson)
        )
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is DomainException.TimeoutException || ex is DomainException.NetworkException,
            "Expected TimeoutException or NetworkException but got ${ex?.javaClass?.simpleName}"
        )
    }

    // Test 18
    @Test
    fun should_throwNetworkException_when_down() = runTest {
        mockWebServer.shutdown()
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
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
    fun should_handleNullBody_when_200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("null")
                .addHeader("Content-Type", "application/json")
        )
        val result = apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    // Test 20
    @Test
    fun should_sendFileName_when_uploading() = runTest {
        enqueueJson(200, successJson)
        apiClient.uploadRecording(1L, "CUSTOMER_MEETING", "테스트 회의", tempFile)
        val request = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains(tempFile.name), "Expected filename ${tempFile.name} in body")
    }
}
