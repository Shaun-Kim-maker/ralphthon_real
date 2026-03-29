package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.UploadRepository
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
class UploadConversationUseCaseTest {

    @MockK
    private lateinit var repository: UploadRepository
    private lateinit var useCase: UploadConversationUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = UploadConversationUseCase(repository)
    }

    private fun makeConversation(
        id: Long = 1L,
        customerId: Long = 1L,
        title: String = "테스트 대화",
        date: String = "2026-03-29",
        type: ConversationType = ConversationType.CUSTOMER_MEETING
    ) = Conversation(
        id = id,
        customerId = customerId,
        title = title,
        date = date,
        type = type,
        duration = 60,
        summary = "요약",
        sentiment = Sentiment.POSITIVE,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = emptyList()
    )

    // ===== Success (1-7) =====

    @Test
    fun should_returnConversation_when_uploadSucceeds() = runTest {
        val conversation = makeConversation()
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "테스트", "/path/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "테스트", "/path/file.mp3")

        assertTrue(result.isSuccess)
        assertEquals(conversation, result.getOrNull())
    }

    @Test
    fun should_passCustomerId_when_uploading() = runTest {
        val conversation = makeConversation(customerId = 42L)
        coEvery { repository.uploadRecording(42L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(42L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(42L, any(), any(), any()) }
    }

    @Test
    fun should_passConversationType_when_customerMeeting() = runTest {
        val conversation = makeConversation(type = ConversationType.CUSTOMER_MEETING)
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(any(), ConversationType.CUSTOMER_MEETING, any(), any()) }
    }

    @Test
    fun should_passConversationType_when_internalMeeting() = runTest {
        val conversation = makeConversation(type = ConversationType.INTERNAL_MEETING)
        coEvery { repository.uploadRecording(1L, ConversationType.INTERNAL_MEETING, "내부회의", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.INTERNAL_MEETING, "내부회의", "/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(any(), ConversationType.INTERNAL_MEETING, any(), any()) }
    }

    @Test
    fun should_passTitle_when_uploading() = runTest {
        val conversation = makeConversation(title = "정확한 제목")
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "정확한 제목", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "정확한 제목", "/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(any(), any(), "정확한 제목", any()) }
    }

    @Test
    fun should_passFilePath_when_uploading() = runTest {
        val conversation = makeConversation()
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목", "/storage/audio/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/storage/audio/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(any(), any(), any(), "/storage/audio/file.mp3") }
    }

    @Test
    fun should_trimTitle_when_hasWhitespace() = runTest {
        val conversation = makeConversation(title = "제목")
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "  제목  ", "/file.mp3")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.uploadRecording(any(), any(), "제목", any()) }
    }

    // ===== Validation (8-17) =====

    @Test
    fun should_returnFailure_when_titleEmpty() = runTest {
        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_returnFailure_when_titleBlank() = runTest {
        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "   ", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_returnFailure_when_filePathEmpty() = runTest {
        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_returnFailure_when_filePathBlank() = runTest {
        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_returnFailure_when_customerIdZero() = runTest {
        val result = useCase(0L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_returnFailure_when_customerIdNegative() = runTest {
        val result = useCase(-1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun should_notCallRepo_when_titleEmpty() = runTest {
        useCase(1L, ConversationType.CUSTOMER_MEETING, "", "/file.mp3")

        coVerify(exactly = 0) { repository.uploadRecording(any(), any(), any(), any()) }
    }

    @Test
    fun should_notCallRepo_when_filePathBlank() = runTest {
        useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "   ")

        coVerify(exactly = 0) { repository.uploadRecording(any(), any(), any(), any()) }
    }

    @Test
    fun should_notCallRepo_when_customerIdInvalid() = runTest {
        useCase(0L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        coVerify(exactly = 0) { repository.uploadRecording(any(), any(), any(), any()) }
    }

    @Test
    fun should_acceptLongTitle_when_valid() = runTest {
        val longTitle = "a".repeat(200)
        val conversation = makeConversation(title = longTitle)
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, longTitle, "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, longTitle, "/file.mp3")

        assertTrue(result.isSuccess)
    }

    // ===== Error cases (18-25) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.ServerException)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_unknownError() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.UnknownException())

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    @Test
    fun should_returnFailure_when_fileTooLarge() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.failure(DomainException.ServerException(413))

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(413, ex.code)
    }

    @Test
    fun should_propagateException_when_repoThrows() = runTest {
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } throws RuntimeException("unexpected")

        var caught = false
        try {
            useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")
        } catch (e: RuntimeException) {
            caught = true
            assertEquals("unexpected", e.message)
        }
        assertTrue(caught)
    }

    // ===== Result fields (26-30) =====

    @Test
    fun should_returnConversationId_when_success() = runTest {
        val conversation = makeConversation(id = 99L)
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertEquals(99L, result.getOrNull()!!.id)
    }

    @Test
    fun should_returnTitle_when_success() = runTest {
        val conversation = makeConversation(title = "반환된 제목")
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "반환된 제목", "/file.mp3")

        assertEquals("반환된 제목", result.getOrNull()!!.title)
    }

    @Test
    fun should_returnType_when_success() = runTest {
        val conversation = makeConversation(type = ConversationType.INTERNAL_MEETING)
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.INTERNAL_MEETING, "제목", "/file.mp3")

        assertEquals(ConversationType.INTERNAL_MEETING, result.getOrNull()!!.type)
    }

    @Test
    fun should_returnDate_when_success() = runTest {
        val conversation = makeConversation(date = "2026-03-29")
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertEquals("2026-03-29", result.getOrNull()!!.date)
    }

    @Test
    fun should_returnSummary_when_success() = runTest {
        val conversation = makeConversation()
        coEvery { repository.uploadRecording(any(), any(), any(), any()) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertEquals("요약", result.getOrNull()!!.summary)
    }

    // ===== Edge (31-35) =====

    @Test
    fun should_handleKoreanTitle_when_uploading() = runTest {
        val conversation = makeConversation(title = "한국어 제목 테스트")
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "한국어 제목 테스트", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "한국어 제목 테스트", "/file.mp3")

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleSpecialCharsInTitle_when_uploading() = runTest {
        val title = "제목 !@#"
        val conversation = makeConversation(title = title)
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, title, "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, title, "/file.mp3")

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleLongFilePath_when_valid() = runTest {
        val longPath = "/storage/" + "a".repeat(300) + ".mp3"
        val conversation = makeConversation()
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목", longPath) } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", longPath)

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleCustomerMeeting_when_typeSpecified() = runTest {
        val conversation = makeConversation(type = ConversationType.CUSTOMER_MEETING)
        coEvery { repository.uploadRecording(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/file.mp3")

        assertTrue(result.isSuccess)
        assertEquals(ConversationType.CUSTOMER_MEETING, result.getOrNull()!!.type)
    }

    @Test
    fun should_handleInternalMeeting_when_typeSpecified() = runTest {
        val conversation = makeConversation(type = ConversationType.INTERNAL_MEETING)
        coEvery { repository.uploadRecording(1L, ConversationType.INTERNAL_MEETING, "내부회의", "/file.mp3") } returns Result.success(conversation)

        val result = useCase(1L, ConversationType.INTERNAL_MEETING, "내부회의", "/file.mp3")

        assertTrue(result.isSuccess)
        assertEquals(ConversationType.INTERNAL_MEETING, result.getOrNull()!!.type)
    }
}
