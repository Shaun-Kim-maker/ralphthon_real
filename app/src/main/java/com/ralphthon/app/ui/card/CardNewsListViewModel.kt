package com.ralphthon.app.ui.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.usecase.GetCardsByCustomerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardNewsListUiState {
    object Loading : CardNewsListUiState()
    data class Data(
        val cards: List<ContextCard>,
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false,
        val currentFilter: ConversationType? = null
    ) : CardNewsListUiState()
    object Empty : CardNewsListUiState()
    data class Error(val message: String) : CardNewsListUiState()
}

sealed class CardNewsListEvent {
    data class NavigateToDetail(val cardId: Long) : CardNewsListEvent()
}

@HiltViewModel
class CardNewsListViewModel @Inject constructor(
    private val getCardsByCustomerUseCase: GetCardsByCustomerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _uiState = MutableStateFlow<CardNewsListUiState>(CardNewsListUiState.Loading)
    val uiState: StateFlow<CardNewsListUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<CardNewsListEvent?>(null)
    val event: StateFlow<CardNewsListEvent?> = _event.asStateFlow()

    private var currentPage = 0
    private var currentFilter: ConversationType? = null
    private val pageSize = 10

    init {
        loadCards()
    }

    fun loadCards() {
        _uiState.value = CardNewsListUiState.Loading
        currentPage = 0
        viewModelScope.launch {
            getCardsByCustomerUseCase.getFiltered(customerId, conversationType = currentFilter).fold(
                onSuccess = { cards ->
                    _uiState.value = if (cards.isEmpty()) {
                        CardNewsListUiState.Empty
                    } else {
                        CardNewsListUiState.Data(cards = cards, hasMore = cards.size >= pageSize, currentFilter = currentFilter)
                    }
                },
                onFailure = { error ->
                    _uiState.value = CardNewsListUiState.Error(mapErrorMessage(error))
                }
            )
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState !is CardNewsListUiState.Data || !currentState.hasMore || currentState.isLoadingMore) return

        _uiState.value = currentState.copy(isLoadingMore = true)
        viewModelScope.launch {
            currentPage++
            getCardsByCustomerUseCase(customerId, currentPage, pageSize).fold(
                onSuccess = { newCards ->
                    _uiState.value = currentState.copy(
                        cards = currentState.cards + newCards,
                        hasMore = newCards.size >= pageSize,
                        isLoadingMore = false
                    )
                },
                onFailure = {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    currentPage--
                }
            )
        }
    }

    fun filterByType(type: ConversationType?) {
        currentFilter = type
        loadCards()
    }

    fun onCardClick(cardId: Long) {
        _event.value = CardNewsListEvent.NavigateToDetail(cardId)
    }

    fun onEventConsumed() {
        _event.value = null
    }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is DomainException.NetworkException -> "서버 연결에 실패했습니다"
        is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
        is DomainException.ServerException -> "서버 오류가 발생했습니다"
        is DomainException.UnauthorizedException -> "인증에 실패했습니다"
        is DomainException.NotFoundException -> "데이터를 찾을 수 없습니다"
        else -> "알 수 없는 오류가 발생했습니다"
    }
}
