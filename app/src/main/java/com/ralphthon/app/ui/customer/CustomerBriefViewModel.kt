package com.ralphthon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.CustomerBrief
import com.ralphthon.app.domain.usecase.GetCustomerBriefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CustomerBriefUiState {
    object Loading : CustomerBriefUiState()
    data class Data(
        val brief: CustomerBrief,
        val isPredictionsExpanded: Boolean = false,
        val isPriceHistoryExpanded: Boolean = false,
        val isActionItemsExpanded: Boolean = false
    ) : CustomerBriefUiState()
    data class Error(val message: String) : CustomerBriefUiState()
}

@HiltViewModel
class CustomerBriefViewModel @Inject constructor(
    private val getCustomerBriefUseCase: GetCustomerBriefUseCase
) : ViewModel() {

    private var customerId: Long = 0L

    private val _uiState = MutableStateFlow<CustomerBriefUiState>(CustomerBriefUiState.Loading)
    val uiState: StateFlow<CustomerBriefUiState> = _uiState.asStateFlow()

    fun loadBrief(customerId: Long) {
        this.customerId = customerId
        viewModelScope.launch {
            _uiState.value = CustomerBriefUiState.Loading
            getCustomerBriefUseCase(customerId).fold(
                onSuccess = { brief -> _uiState.value = CustomerBriefUiState.Data(brief) },
                onFailure = { error -> _uiState.value = CustomerBriefUiState.Error(mapErrorMessage(error)) }
            )
        }
    }

    fun refresh() { loadBrief(customerId) }
    fun retry() { loadBrief(customerId) }

    fun togglePredictions() { updateState { copy(isPredictionsExpanded = !isPredictionsExpanded) } }
    fun togglePriceHistory() { updateState { copy(isPriceHistoryExpanded = !isPriceHistoryExpanded) } }
    fun toggleActionItems() { updateState { copy(isActionItemsExpanded = !isActionItemsExpanded) } }

    private fun updateState(update: CustomerBriefUiState.Data.() -> CustomerBriefUiState.Data) {
        val current = _uiState.value
        if (current is CustomerBriefUiState.Data) { _uiState.value = current.update() }
    }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is DomainException.NetworkException -> "서버 연결에 실패했습니다"
        is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
        is DomainException.NotFoundException -> "고객 정보를 찾을 수 없습니다"
        is DomainException.ServerException -> "서버 오류가 발생했습니다"
        else -> "알 수 없는 오류가 발생했습니다"
    }
}
