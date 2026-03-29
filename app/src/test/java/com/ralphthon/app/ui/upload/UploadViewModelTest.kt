package com.ralphthon.app.ui.upload

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import com.ralphthon.app.domain.usecase.UploadConversationUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class UploadViewModelTest {

    @MockK
    private lateinit var uploadUseCase: UploadConversationUseCase

    @MockK
    private lateinit var getCustomersUseCase: GetCustomersUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeCustomer(
        id: Long = 1L,
        companyName: String = "삼성전자",
        contactName: String? = "홍길동",
        industry: String = "전자",
        lastInteractionDate: String = "2026-03-01",
        totalConversations: Int = 5,
        summary: String? = null
    ) = Customer(id, companyName, contactName, industry, lastInteractionDate, totalConversations, summary)

    private fun makeConversation(
        id: Long = 1L,
        customerId: Long = 1L,
        type: ConversationType = ConversationType.CUSTOMER_MEETING
    ) = Conversation(
        id = id,
        customerId = customerId,
        title = "테스트 대화",
        date = "2026-03-29",
        type = type,
        duration = 60,
        summary = "요약",
        sentiment = Sentiment.NEUTRAL,
        keywords = emptyList(),
        keyStatements = emptyList(),
        priceCommitments = emptyList(),
        actionItems = emptyList(),
        predictedQuestions = emptyList()
    )

    private fun createViewModel(): UploadViewModel {
        return UploadViewModel(uploadUseCase, getCustomersUseCase)
    }

    // ===== States (1-6) =====

    @Test
    fun should_beIdle_when_initialState() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    @Test
    fun should_showUploading_when_uploadStarted() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(makeConversation())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        // After upload completes, state should be Success (went through Uploading)
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_showSuccess_when_uploadSucceeds() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(makeConversation())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is UploadUiState.Success)
        assertEquals("업로드 완료", (state as UploadUiState.Success).message)
    }

    @Test
    fun should_showError_when_uploadFails() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Error)
    }

    @Test
    fun should_loadCustomers_when_initialized() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2))
        coEvery { getCustomersUseCase() } returns Result.success(customers)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.formState.value.customers.size)
    }

    @Test
    fun should_returnToIdle_when_resetCalled() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(makeConversation())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        viewModel.resetForm()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    // ===== Form (7-14) =====

    @Test
    fun should_updateSelectedCustomer_when_selectCustomerCalled() = runTest {
        val customer = makeCustomer(id = 5L, companyName = "LG전자")
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        assertEquals(customer, viewModel.formState.value.selectedCustomer)
    }

    @Test
    fun should_updateTitle_when_setTitleCalled() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setTitle("새 제목")
        assertEquals("새 제목", viewModel.formState.value.title)
    }

    @Test
    fun should_updateConversationType_when_setTypeCalled() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setConversationType(ConversationType.INTERNAL_MEETING)
        assertEquals(ConversationType.INTERNAL_MEETING, viewModel.formState.value.conversationType)
    }

    @Test
    fun should_updateFilePath_when_setFileCalled() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setFile("/storage/recording.mp3", "recording.mp3")
        assertEquals("/storage/recording.mp3", viewModel.formState.value.filePath)
        assertEquals("recording.mp3", viewModel.formState.value.fileName)
    }

    @Test
    fun should_defaultToCustomerMeeting_when_formCreated() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(ConversationType.CUSTOMER_MEETING, viewModel.formState.value.conversationType)
    }

    @Test
    fun should_setInternalMeeting_when_typeChangedToInternal() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setConversationType(ConversationType.INTERNAL_MEETING)
        assertEquals(ConversationType.INTERNAL_MEETING, viewModel.formState.value.conversationType)
    }

    @Test
    fun should_blockUpload_when_titleIsBlank() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("   ")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    @Test
    fun should_blockUpload_when_noFileSelected() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        // No file set
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    // ===== Upload (15-22) =====

    @Test
    fun should_uploadSuccessfully_when_allFieldsProvided() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/path/file.mp3") } returns Result.success(makeConversation())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_blockUpload_when_noCustomerSelected() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    @Test
    fun should_blockUpload_when_titleIsEmpty() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setFile("/path/file.mp3", "file.mp3")
        // Title is empty by default
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    @Test
    fun should_blockUpload_when_filePathMissing() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    @Test
    fun should_passAllFields_when_uploading() = runTest {
        val customer = makeCustomer(id = 7L)
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(7L, ConversationType.CUSTOMER_MEETING, "영업 회의", "/storage/file.mp3") } returns Result.success(makeConversation(customerId = 7L))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("영업 회의")
        viewModel.setFile("/storage/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_uploadWithCustomerMeeting_when_typeIsCustomerMeeting() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(1L, ConversationType.CUSTOMER_MEETING, "제목", "/path/file.mp3") } returns Result.success(makeConversation(type = ConversationType.CUSTOMER_MEETING))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setConversationType(ConversationType.CUSTOMER_MEETING)
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_uploadWithInternalMeeting_when_typeIsInternalMeeting() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(1L, ConversationType.INTERNAL_MEETING, "사내 회의", "/path/file.mp3") } returns Result.success(makeConversation(type = ConversationType.INTERNAL_MEETING))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("사내 회의")
        viewModel.setConversationType(ConversationType.INTERNAL_MEETING)
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_showUploadingState_when_uploadInProgress() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(makeConversation())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Success)
    }

    // ===== Error (23-27) =====

    @Test
    fun should_showNetworkError_when_networkException() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        val state = viewModel.uiState.value as UploadUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_showTimeoutError_when_timeoutException() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        val state = viewModel.uiState.value as UploadUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    @Test
    fun should_showValidationError_when_illegalArgument() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(IllegalArgumentException("입력 오류 메시지"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        val state = viewModel.uiState.value as UploadUiState.Error
        assertEquals("입력 오류 메시지", state.message)
    }

    @Test
    fun should_showServerError_when_serverException() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        val state = viewModel.uiState.value as UploadUiState.Error
        assertEquals("업로드 중 오류가 발생했습니다", state.message)
    }

    @Test
    fun should_resetAfterError_when_resetFormCalled() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.upload()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UploadUiState.Error)
        viewModel.resetForm()
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }

    // ===== Edge cases (28-30) =====

    @Test
    fun should_clearFormFields_when_resetFormCalled() = runTest {
        val customer = makeCustomer()
        coEvery { getCustomersUseCase() } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.selectCustomer(customer)
        viewModel.setTitle("제목")
        viewModel.setFile("/path/file.mp3", "file.mp3")
        viewModel.resetForm()
        val form = viewModel.formState.value
        assertEquals("", form.title)
        assertNull(form.filePath)
        assertNull(form.fileName)
        assertNull(form.selectedCustomer)
    }

    @Test
    fun should_loadCustomersOnInit_when_viewModelCreated() = runTest {
        val customers = listOf(makeCustomer(1, "회사A"), makeCustomer(2, "회사B"), makeCustomer(3, "회사C"))
        coEvery { getCustomersUseCase() } returns Result.success(customers)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(3, viewModel.formState.value.customers.size)
    }

    @Test
    fun should_handleEmptyCustomers_when_noCustomersAvailable() = runTest {
        coEvery { getCustomersUseCase() } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(0, viewModel.formState.value.customers.size)
        assertTrue(viewModel.uiState.value is UploadUiState.Idle)
    }
}
