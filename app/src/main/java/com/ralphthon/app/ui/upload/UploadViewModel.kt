package com.ralphthon.app.ui.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCustomersUseCase
import com.ralphthon.app.domain.usecase.UploadConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Uploading : UploadUiState()
    data class Success(val message: String) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

data class UploadFormState(
    val selectedCustomer: Customer? = null,
    val customers: List<Customer> = emptyList(),
    val title: String = "",
    val conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
    val filePath: String? = null,
    val fileName: String? = null
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadUseCase: UploadConversationUseCase,
    private val getCustomersUseCase: GetCustomersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(UploadFormState())
    val formState: StateFlow<UploadFormState> = _formState.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            getCustomersUseCase().fold(
                onSuccess = { customers ->
                    _formState.value = _formState.value.copy(customers = customers)
                },
                onFailure = { }
            )
        }
    }

    fun selectCustomer(customer: Customer) {
        _formState.value = _formState.value.copy(selectedCustomer = customer)
    }

    fun setTitle(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun setConversationType(type: ConversationType) {
        _formState.value = _formState.value.copy(conversationType = type)
    }

    fun setFile(path: String, name: String) {
        _formState.value = _formState.value.copy(filePath = path, fileName = name)
    }

    fun upload() {
        val form = _formState.value
        val customer = form.selectedCustomer ?: return
        val filePath = form.filePath ?: return
        if (form.title.isBlank()) return

        viewModelScope.launch {
            _uiState.value = UploadUiState.Uploading
            uploadUseCase(customer.id, form.conversationType, form.title, filePath).fold(
                onSuccess = { _uiState.value = UploadUiState.Success("업로드 완료") },
                onFailure = { error -> _uiState.value = UploadUiState.Error(mapErrorMessage(error)) }
            )
        }
    }

    fun resetForm() {
        _uiState.value = UploadUiState.Idle
        _formState.value = _formState.value.copy(
            title = "",
            filePath = null,
            fileName = null,
            selectedCustomer = null
        )
    }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is DomainException.NetworkException -> "서버 연결에 실패했습니다"
        is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
        is IllegalArgumentException -> error.message ?: "입력 오류"
        else -> "업로드 중 오류가 발생했습니다"
    }
}
