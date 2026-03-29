package com.ralphthon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CustomerListUiState {
    object Loading : CustomerListUiState()
    data class Data(val customers: List<Customer>) : CustomerListUiState()
    object Empty : CustomerListUiState()
    data class Error(val message: String) : CustomerListUiState()
}

sealed class CustomerListEvent {
    data class NavigateToCards(val customerId: Long) : CustomerListEvent()
    data class ShowSnackbar(val message: String) : CustomerListEvent()
}

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerListUiState>(CustomerListUiState.Loading)
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<CustomerListEvent?>(null)
    val event: StateFlow<CustomerListEvent?> = _event.asStateFlow()

    private var currentSortBy: GetCustomersUseCase.SortBy = GetCustomersUseCase.SortBy.LAST_INTERACTION

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomerListUiState.Loading
            getCustomersUseCase.getSorted(currentSortBy).fold(
                onSuccess = { customers ->
                    _uiState.value = if (customers.isEmpty()) {
                        CustomerListUiState.Empty
                    } else {
                        CustomerListUiState.Data(customers)
                    }
                },
                onFailure = { error ->
                    _uiState.value = CustomerListUiState.Error(mapErrorMessage(error))
                }
            )
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        viewModelScope.launch {
            getCustomersUseCase.getSorted(currentSortBy).fold(
                onSuccess = { customers ->
                    _uiState.value = if (customers.isEmpty()) {
                        CustomerListUiState.Empty
                    } else {
                        CustomerListUiState.Data(customers)
                    }
                },
                onFailure = { error ->
                    if (currentState is CustomerListUiState.Data) {
                        _event.value = CustomerListEvent.ShowSnackbar(mapErrorMessage(error))
                    } else {
                        _uiState.value = CustomerListUiState.Error(mapErrorMessage(error))
                    }
                }
            )
        }
    }

    fun retry() {
        loadCustomers()
    }

    fun onCustomerClick(customerId: Long) {
        _event.value = CustomerListEvent.NavigateToCards(customerId)
    }

    fun onEventConsumed() {
        _event.value = null
    }

    fun sortBy(sortBy: GetCustomersUseCase.SortBy) {
        currentSortBy = sortBy
        loadCustomers()
    }

    private fun mapErrorMessage(error: Throwable): String {
        return when (error) {
            is DomainException.NetworkException -> "서버 연결에 실패했습니다"
            is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
            is DomainException.ServerException -> "서버 오류가 발생했습니다"
            is DomainException.UnauthorizedException -> "인증에 실패했습니다"
            is DomainException.NotFoundException -> "데이터를 찾을 수 없습니다"
            else -> "알 수 없는 오류가 발생했습니다"
        }
    }
}
