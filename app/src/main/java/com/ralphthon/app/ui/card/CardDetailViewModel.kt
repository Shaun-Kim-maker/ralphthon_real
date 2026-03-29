package com.ralphthon.app.ui.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.usecase.GetCardDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardDetailUiState {
    object Loading : CardDetailUiState()
    data class Data(
        val card: ContextCard,
        val additionalKnowledge: List<KnowledgeArticle>,
        val isPriceExpanded: Boolean = false,
        val isActionExpanded: Boolean = false,
        val isPredictionExpanded: Boolean = false,
        val isKnowledgeExpanded: Boolean = false
    ) : CardDetailUiState()
    data class Error(val message: String) : CardDetailUiState()
}

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getCardDetailUseCase: GetCardDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardId: Long = savedStateHandle.get<Long>("cardId") ?: 0L

    private val _uiState = MutableStateFlow<CardDetailUiState>(CardDetailUiState.Loading)
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init { loadCardDetail() }

    fun loadCardDetail() {
        viewModelScope.launch {
            _uiState.value = CardDetailUiState.Loading
            getCardDetailUseCase(cardId).fold(
                onSuccess = { result ->
                    _uiState.value = CardDetailUiState.Data(
                        card = result.card,
                        additionalKnowledge = result.additionalKnowledge
                    )
                },
                onFailure = { error ->
                    _uiState.value = CardDetailUiState.Error(mapErrorMessage(error))
                }
            )
        }
    }

    fun togglePricePanel() { updatePanel { copy(isPriceExpanded = !isPriceExpanded) } }
    fun toggleActionPanel() { updatePanel { copy(isActionExpanded = !isActionExpanded) } }
    fun togglePredictionPanel() { updatePanel { copy(isPredictionExpanded = !isPredictionExpanded) } }
    fun toggleKnowledgePanel() { updatePanel { copy(isKnowledgeExpanded = !isKnowledgeExpanded) } }

    private fun updatePanel(update: CardDetailUiState.Data.() -> CardDetailUiState.Data) {
        val current = _uiState.value
        if (current is CardDetailUiState.Data) {
            _uiState.value = current.update()
        }
    }

    fun retry() { loadCardDetail() }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is DomainException.NetworkException -> "서버 연결에 실패했습니다"
        is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
        is DomainException.NotFoundException -> "카드를 찾을 수 없습니다"
        is DomainException.ServerException -> "서버 오류가 발생했습니다"
        else -> "알 수 없는 오류가 발생했습니다"
    }
}
