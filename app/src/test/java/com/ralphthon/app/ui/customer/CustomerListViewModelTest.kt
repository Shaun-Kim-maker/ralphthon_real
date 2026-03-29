package com.ralphthon.app.ui.customer

import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
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
class CustomerListViewModelTest {

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
        summary: String? = "요약"
    ) = Customer(id, companyName, contactName, industry, lastInteractionDate, totalConversations, summary)

    private fun createViewModel(): CustomerListViewModel {
        return CustomerListViewModel(getCustomersUseCase)
    }

    // ===== Loading/Data/Empty/Error states (1-12) =====

    @Test
    fun should_showLoading_when_screenOpened() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val viewModel = createViewModel()
        // Before coroutine runs, state is Loading
        assertEquals(CustomerListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_showCustomerList_when_apiReturnsData() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2), makeCustomer(3))
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(customers)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Data)
        assertEquals(3, (state as CustomerListUiState.Data).customers.size)
    }

    @Test
    fun should_showEmpty_when_apiReturnsEmptyList() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_showError_when_apiThrowsNetworkException() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NetworkException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("서버 연결에 실패했습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showError_when_apiTimesOut() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.TimeoutException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("서버 응답 시간이 초과되었습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showError_when_serverError() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.ServerException(500))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showError_when_unauthorized() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.UnauthorizedException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("인증에 실패했습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showError_when_notFound() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.NotFoundException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("데이터를 찾을 수 없습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showError_when_unknownError() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.UnknownException())
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Error)
        assertEquals("알 수 없는 오류가 발생했습니다", (state as CustomerListUiState.Error).message)
    }

    @Test
    fun should_showData_when_10CustomersReturned() = runTest {
        val customers = (1L..10L).map { makeCustomer(id = it) }
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(customers)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Data)
        assertEquals(10, (state as CustomerListUiState.Data).customers.size)
    }

    @Test
    fun should_preserveCustomerFields_when_dataShown() = runTest {
        val customer = makeCustomer(
            id = 42L,
            companyName = "LG전자",
            contactName = "김철수",
            industry = "전자",
            lastInteractionDate = "2026-03-15",
            totalConversations = 10,
            summary = "상세 요약"
        )
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        val c = state.customers.first()
        assertEquals(42L, c.id)
        assertEquals("LG전자", c.companyName)
        assertEquals("김철수", c.contactName)
        assertEquals("전자", c.industry)
        assertEquals("2026-03-15", c.lastInteractionDate)
        assertEquals(10, c.totalConversations)
        assertEquals("상세 요약", c.summary)
    }

    @Test
    fun should_handleNullContactName_when_customerHasNull() = runTest {
        val customer = makeCustomer(contactName = null)
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(customer))
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertNull(state.customers.first().contactName)
    }

    // ===== Refresh (13-18) =====

    @Test
    fun should_refreshData_when_pullToRefresh() = runTest {
        val initial = listOf(makeCustomer(1))
        val refreshed = listOf(makeCustomer(2), makeCustomer(3))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(initial),
            Result.success(refreshed)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals(2, state.customers.size)
    }

    @Test
    fun should_preserveData_when_refreshFails() = runTest {
        val initial = listOf(makeCustomer(1))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(initial),
            Result.failure(DomainException.NetworkException())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        // State should remain Data (not switched to Error)
        assertTrue(viewModel.uiState.value is CustomerListUiState.Data)
        // And a snackbar event should be emitted
        val event = viewModel.event.value
        assertTrue(event is CustomerListEvent.ShowSnackbar)
    }

    @Test
    fun should_showError_when_refreshFailsFromEmpty() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.failure(DomainException.NetworkException())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerListUiState.Error)
    }

    @Test
    fun should_showLoading_when_loadCustomersCalled() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val viewModel = createViewModel()
        // Initial state before coroutine runs is Loading
        assertEquals(CustomerListUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
    }

    @Test
    fun should_updateData_when_refreshSucceeds() = runTest {
        val old = listOf(makeCustomer(id = 1, companyName = "Old"))
        val new = listOf(makeCustomer(id = 2, companyName = "New"))
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(old),
            Result.success(new)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals("New", state.customers.first().companyName)
    }

    @Test
    fun should_showEmpty_when_refreshReturnsEmpty() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(listOf(makeCustomer())),
            Result.success(emptyList())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, viewModel.uiState.value)
    }

    // ===== Retry (19-21) =====

    @Test
    fun should_retryLoad_when_retryClicked() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCustomer()))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerListUiState.Error)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerListUiState.Data)
    }

    @Test
    fun should_showData_when_retrySucceeds() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.failure(DomainException.NetworkException()),
            Result.success(listOf(makeCustomer(id = 5)))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals(5L, state.customers.first().id)
    }

    @Test
    fun should_showError_when_retryFails() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.failure(DomainException.ServerException(503))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CustomerListUiState.Error)
        assertEquals("서버 오류가 발생했습니다", (viewModel.uiState.value as CustomerListUiState.Error).message)
    }

    // ===== Navigation (22-25) =====

    @Test
    fun should_navigateToCards_when_customerClicked() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCustomerClick(1L)
        val event = viewModel.event.value
        assertTrue(event is CustomerListEvent.NavigateToCards)
    }

    @Test
    fun should_includeCustomerId_when_navigating() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCustomerClick(42L)
        val event = viewModel.event.value as CustomerListEvent.NavigateToCards
        assertEquals(42L, event.customerId)
    }

    @Test
    fun should_clearEvent_when_consumed() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(listOf(makeCustomer()))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCustomerClick(1L)
        viewModel.onEventConsumed()
        assertNull(viewModel.event.value)
    }

    @Test
    fun should_emitSnackbar_when_refreshFailsWithData() = runTest {
        val initial = listOf(makeCustomer())
        coEvery { getCustomersUseCase.getSorted(any()) } returnsMany listOf(
            Result.success(initial),
            Result.failure(DomainException.NetworkException())
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        val event = viewModel.event.value
        assertTrue(event is CustomerListEvent.ShowSnackbar)
        assertEquals("서버 연결에 실패했습니다", (event as CustomerListEvent.ShowSnackbar).message)
    }

    // ===== Sort (26-30) =====

    @Test
    fun should_sortByLastInteraction_when_defaultOrder() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, lastInteractionDate = "2026-01-01"),
            makeCustomer(id = 2, lastInteractionDate = "2026-03-01"),
            makeCustomer(id = 3, lastInteractionDate = "2026-02-01")
        )
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(
            customers.sortedByDescending { it.lastInteractionDate }
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals(2L, state.customers.first().id)
    }

    @Test
    fun should_showData_when_sortedByName() = runTest {
        val sorted = listOf(
            makeCustomer(id = 2, companyName = "기아"),
            makeCustomer(id = 3, companyName = "삼성"),
            makeCustomer(id = 1, companyName = "현대")
        )
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(sorted)
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.NAME) } returns Result.success(sorted)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals("기아", state.customers.first().companyName)
    }

    @Test
    fun should_showData_when_sortedByConversations() = runTest {
        val sorted = listOf(
            makeCustomer(id = 2, totalConversations = 10),
            makeCustomer(id = 3, totalConversations = 7),
            makeCustomer(id = 1, totalConversations = 3)
        )
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(sorted)
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.CONVERSATIONS) } returns Result.success(sorted)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.sortBy(GetCustomersUseCase.SortBy.CONVERSATIONS)
        advanceUntilIdle()
        val state = viewModel.uiState.value as CustomerListUiState.Data
        assertEquals(2L, state.customers.first().id)
    }

    @Test
    fun should_handleEmptySort_when_noCustomers() = runTest {
        coEvery { getCustomersUseCase.getSorted(any()) } returns Result.success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        assertEquals(CustomerListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun should_preserveSort_when_refreshed() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2))
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION) } returns Result.success(customers)
        coEvery { getCustomersUseCase.getSorted(GetCustomersUseCase.SortBy.NAME) } returns Result.success(customers)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.sortBy(GetCustomersUseCase.SortBy.NAME)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        // After refresh, should still use NAME sort
        val state = viewModel.uiState.value
        assertTrue(state is CustomerListUiState.Data)
    }
}
