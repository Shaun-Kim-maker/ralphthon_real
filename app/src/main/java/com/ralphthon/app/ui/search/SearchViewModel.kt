package com.ralphthon.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.usecase.SearchCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Data(val results: List<SearchResult>, val query: String) : SearchUiState()
    data class Empty(val query: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCardsUseCase: SearchCardsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private var searchJob: Job? = null
    private val debounceMs = 300L

    fun search(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(debounceMs)
            _uiState.value = SearchUiState.Loading
            searchCardsUseCase(trimmed).fold(
                onSuccess = { results ->
                    addRecentSearch(trimmed)
                    _uiState.value = if (results.isEmpty()) {
                        SearchUiState.Empty(trimmed)
                    } else {
                        SearchUiState.Data(results, trimmed)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(mapErrorMessage(error))
                }
            )
        }
    }

    fun searchFiltered(query: String, type: String?) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            searchCardsUseCase.searchFiltered(trimmed, type).fold(
                onSuccess = { results ->
                    addRecentSearch(trimmed)
                    _uiState.value = if (results.isEmpty()) {
                        SearchUiState.Empty(trimmed)
                    } else {
                        SearchUiState.Data(results, trimmed)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(mapErrorMessage(error))
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState.Idle
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.value = _recentSearches.value.filter { it != query }
    }

    private fun addRecentSearch(query: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        _recentSearches.value = current.take(10)
    }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is DomainException.NetworkException -> "서버 연결에 실패했습니다"
        is DomainException.TimeoutException -> "서버 응답 시간이 초과되었습니다"
        is DomainException.ServerException -> "서버 오류가 발생했습니다"
        else -> "검색 중 오류가 발생했습니다"
    }
}
