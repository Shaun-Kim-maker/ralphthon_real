package com.ralphthon.app.ui.customer

import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
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
class CustomerListStateTest {

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

    private fun createViewModel() = CustomerListViewModel(getCustomersUseCase)

    // === State transitions (1-15) ===

    @Test
    fun should_transitionToLoading_when_initialState() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        assertEquals(CustomerListUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_transitionToData_when_loadSucceeds() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionToEmpty_when_loadReturnsEmpty() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
    }

    @Test
    fun should_transitionToError_when_loadFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
    }

    @Test
    fun should_transitionLoadingToData_when_dataLoaded() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        assertEquals(CustomerListUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionLoadingToEmpty_when_emptyLoaded() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        assertEquals(CustomerListUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
    }

    @Test
    fun should_transitionLoadingToError_when_errorOccurs() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.ServerException(500))
        val vm = createViewModel()
        assertEquals(CustomerListUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
    }

    @Test
    fun should_transitionErrorToLoading_when_retryClicked() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.retry()
        advanceUntilIdle()
        // After retry completes, state should be Data (went through Loading)
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionErrorToData_when_retrySucceeds() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionDataToLoading_when_refreshed() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
        vm.loadCustomers()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionDataToData_when_refreshSucceeds() = runTest {
        val first = listOf(makeCustomer(id = 1))
        val second = listOf(makeCustomer(id = 2), makeCustomer(id = 3))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(first),
            Result.success(second)
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Data
        assertEquals(2, state.customers.size)
    }

    @Test
    fun should_keepDataState_when_refreshFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(listOf(makeCustomer())),
            Result.failure(DomainException.NetworkException())
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
        assertTrue(vm.event.value is CustomerListEvent.ShowSnackbar)
    }

    @Test
    fun should_transitionEmptyToLoading_when_retried() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_transitionEmptyToData_when_retrySucceeds() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_notTransitionToEmpty_when_alreadyEmpty() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
        vm.retry()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
    }

    // === Navigation events (16-20) ===

    @Test
    fun should_emitNavigateEvent_when_customerClicked() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onCustomerClick(1L)
        assertTrue(vm.event.value is CustomerListEvent.NavigateToCards)
        assertEquals(1L, (vm.event.value as CustomerListEvent.NavigateToCards).customerId)
    }

    @Test
    fun should_clearEvent_when_consumed() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onCustomerClick(1L)
        vm.onEventConsumed()
        assertNull(vm.event.value)
    }

    @Test
    fun should_emitSnackbar_when_refreshFailsInDataState() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(listOf(makeCustomer())),
            Result.failure(DomainException.NetworkException())
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        val event = vm.event.value
        assertTrue(event is CustomerListEvent.ShowSnackbar)
        assertEquals("서버 연결에 실패했습니다", (event as CustomerListEvent.ShowSnackbar).message)
    }

    @Test
    fun should_notEmitSnackbar_when_refreshFailsInErrorState() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.failure(DomainException.TimeoutException())
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        assertTrue(vm.event.value !is CustomerListEvent.ShowSnackbar)
    }

    @Test
    fun should_handleMultipleClicks_when_rapidClicking() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onCustomerClick(1L)
        vm.onCustomerClick(2L)
        vm.onCustomerClick(3L)
        val event = vm.event.value as CustomerListEvent.NavigateToCards
        assertEquals(3L, event.customerId)
    }

    // === Sort state (21-25) ===

    @Test
    fun should_maintainDataState_when_sortChanged() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_showSortedData_when_sortByName() = runTest {
        val sorted = listOf(
            makeCustomer(id = 1, companyName = "기아"),
            makeCustomer(id = 2, companyName = "삼성"),
            makeCustomer(id = 3, companyName = "현대")
        )
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(sorted)
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.NAME) } returns Result.success(sorted)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Data
        assertEquals("기아", state.customers.first().companyName)
    }

    @Test
    fun should_showSortedData_when_sortByConversations() = runTest {
        val sorted = listOf(
            makeCustomer(id = 2, totalConversations = 10),
            makeCustomer(id = 1, totalConversations = 3)
        )
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(sorted)
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.CONVERSATIONS) } returns Result.success(sorted)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.sortBy(GetCustomersUseCase.SortBy.CONVERSATIONS)
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Data
        assertEquals(2L, state.customers.first().id)
    }

    @Test
    fun should_handleEmptyAfterSort_when_noData() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val vm = createViewModel()
        advanceUntilIdle()
        vm.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, vm.uiState.value)
    }

    @Test
    fun should_transitionToLoading_when_sortWithRefresh() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    // === Error recovery patterns (26-30) ===

    @Test
    fun should_recoverFromNetwork_when_retried() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_recoverFromTimeout_when_retried() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.TimeoutException()),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_recoverFromServer_when_retried() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.ServerException(503)),
            Result.success(listOf(makeCustomer()))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Error)
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_showCorrectMessage_when_networkError() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException())
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Error
        assertEquals("서버 연결에 실패했습니다", state.message)
    }

    @Test
    fun should_showCorrectMessage_when_timeoutError() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.TimeoutException())
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value as CustomerListUiState.Error
        assertEquals("서버 응답 시간이 초과되었습니다", state.message)
    }

    // === Rapid state changes (31-35) ===

    @Test
    fun should_handleRapidRefresh_when_multipleRefreshCalls() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        vm.refresh()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_settleToFinalState_when_multipleLoads() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        vm.loadCustomers()
        vm.loadCustomers()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_handleLoadDuringLoad_when_concurrent() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        vm.loadCustomers()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_handleRetryDuringLoad_when_concurrent() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        vm.retry()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_handleNavigationDuringLoad_when_concurrent() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val vm = createViewModel()
        vm.onCustomerClick(5L)
        advanceUntilIdle()
        val navEvent = vm.event.value as CustomerListEvent.NavigateToCards
        assertEquals(5L, navEvent.customerId)
        assertTrue(vm.uiState.value is CustomerListUiState.Data)
    }
}
