package com.ralphthon.app.ui.upload

import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import com.ralphthon.app.domain.usecase.UploadConversationUseCase
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
class UploadStateTest {

    @MockK
    private lateinit var uploadUseCase: UploadConversationUseCase

    @MockK
    private lateinit var getCustomersUseCase: GetCustomersUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
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
        totalConversations: Int = 5
    ) = Customer(id, companyName, contactName, industry, lastInteractionDate, totalConversations, null)

    private fun createViewModel(): UploadViewModel {
        coEvery { getCustomersUseCase() } returns Result.success(listOf(makeCustomer()))
        return UploadViewModel(uploadUseCase, getCustomersUseCase)
    }

    // === Initial / Idle state (1-4) ===

    @Test
    fun should_beIdle_when_initialState() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_haveDefaultFormState_when_initialState() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val form = vm.formState.value
        assertNull(form.selectedCustomer)
        assertEquals("", form.title)
        assertNull(form.filePath)
        assertNull(form.fileName)
    }

    @Test
    fun should_haveCustomerMeetingAsDefaultType_when_initialState() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(ConversationType.CUSTOMER_MEETING, vm.formState.value.conversationType)
    }

    @Test
    fun should_loadCustomers_when_initialized() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(1, vm.formState.value.customers.size)
    }

    // === Form state updates (5-11) ===

    @Test
    fun should_updateSelectedCustomer_when_selectCustomerCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val customer = makeCustomer(id = 42L)
        vm.selectCustomer(customer)
        assertEquals(customer, vm.formState.value.selectedCustomer)
    }

    @Test
    fun should_updateTitle_when_setTitleCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setTitle("신규 미팅")
        assertEquals("신규 미팅", vm.formState.value.title)
    }

    @Test
    fun should_updateConversationTypeToCustomerMeeting_when_setConversationTypeCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setConversationType(ConversationType.CUSTOMER_MEETING)
        assertEquals(ConversationType.CUSTOMER_MEETING, vm.formState.value.conversationType)
    }

    @Test
    fun should_updateConversationTypeToInternalMeeting_when_setConversationTypeCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setConversationType(ConversationType.INTERNAL_MEETING)
        assertEquals(ConversationType.INTERNAL_MEETING, vm.formState.value.conversationType)
    }

    @Test
    fun should_updateFilePath_when_setFileCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFile("/storage/audio.m4a", "audio.m4a")
        assertEquals("/storage/audio.m4a", vm.formState.value.filePath)
    }

    @Test
    fun should_updateFileName_when_setFileCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFile("/storage/audio.m4a", "audio.m4a")
        assertEquals("audio.m4a", vm.formState.value.fileName)
    }

    @Test
    fun should_updateBothFileFields_when_setFileCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFile("/data/meeting.wav", "meeting.wav")
        val form = vm.formState.value
        assertEquals("/data/meeting.wav", form.filePath)
        assertEquals("meeting.wav", form.fileName)
    }

    // === Upload transitions (12-18) ===

    @Test
    fun should_transitionToUploading_when_uploadCalled() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        // Before advanceUntilIdle, state should be Uploading
        assertEquals(UploadUiState.Uploading, vm.uiState.value)
    }

    @Test
    fun should_transitionUploadingToSuccess_when_uploadSucceeds() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_containSuccessMessage_when_uploadSucceeds() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        val state = vm.uiState.value as UploadUiState.Success
        assertEquals("업로드 완료", state.message)
    }

    @Test
    fun should_transitionUploadingToError_when_uploadFails() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Error)
    }

    @Test
    fun should_containNetworkErrorMessage_when_networkExceptionThrown() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        val state = vm.uiState.value as UploadUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_containTimeoutErrorMessage_when_timeoutExceptionThrown() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        val state = vm.uiState.value as UploadUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    @Test
    fun should_containGenericErrorMessage_when_unknownExceptionThrown() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.failure(RuntimeException("unknown"))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        val state = vm.uiState.value as UploadUiState.Error
        assertEquals("업로드 중 오류가 발생했습니다", state.message)
    }

    // === Upload guard conditions (19-22) ===

    @Test
    fun should_notTransitionToUploading_when_noCustomerSelected() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setTitle("미팅 제목")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notTransitionToUploading_when_noFileSelected() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅 제목")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notTransitionToUploading_when_titleIsBlank() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_notTransitionToUploading_when_titleIsWhitespaceOnly() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("   ")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    // === resetForm (23-27) ===

    @Test
    fun should_transitionToIdle_when_resetFormCalled() = runTest {
        coEvery { uploadUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("미팅")
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Success)
        vm.resetForm()
        assertEquals(UploadUiState.Idle, vm.uiState.value)
    }

    @Test
    fun should_clearTitle_when_resetFormCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setTitle("미팅 제목")
        vm.resetForm()
        assertEquals("", vm.formState.value.title)
    }

    @Test
    fun should_clearFilePath_when_resetFormCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.resetForm()
        assertNull(vm.formState.value.filePath)
    }

    @Test
    fun should_clearFileName_when_resetFormCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.resetForm()
        assertNull(vm.formState.value.fileName)
    }

    @Test
    fun should_clearSelectedCustomer_when_resetFormCalled() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.resetForm()
        assertNull(vm.formState.value.selectedCustomer)
    }

    // === ConversationType selection (28-30) ===

    @Test
    fun should_preserveConversationType_when_formReset() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setConversationType(ConversationType.INTERNAL_MEETING)
        vm.resetForm()
        // resetForm does not reset conversationType per ViewModel implementation
        assertEquals(ConversationType.INTERNAL_MEETING, vm.formState.value.conversationType)
    }

    @Test
    fun should_uploadWithCustomerMeetingType_when_typeIsCustomerMeeting() = runTest {
        coEvery { uploadUseCase(any(), eq(ConversationType.CUSTOMER_MEETING), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("고객 미팅")
        vm.setConversationType(ConversationType.CUSTOMER_MEETING)
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Success)
    }

    @Test
    fun should_uploadWithInternalMeetingType_when_typeIsInternalMeeting() = runTest {
        coEvery { uploadUseCase(any(), eq(ConversationType.INTERNAL_MEETING), any(), any()) } returns Result.success(Unit)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectCustomer(makeCustomer())
        vm.setTitle("사내 회의")
        vm.setConversationType(ConversationType.INTERNAL_MEETING)
        vm.setFile("/path/file.m4a", "file.m4a")
        vm.upload()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UploadUiState.Success)
    }
}
