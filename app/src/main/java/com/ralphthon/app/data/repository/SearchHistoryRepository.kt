package com.ralphthon.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

interface SearchHistoryRepository {
    suspend fun addSearch(query: String)
    suspend fun getRecentSearches(limit: Int = 10): List<String>
    suspend fun clearHistory()
}

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor() : SearchHistoryRepository {
    private val history = mutableListOf<String>()

    override suspend fun addSearch(query: String) {
        history.remove(query)
        history.add(0, query)
        if (history.size > 50) history.removeAt(history.lastIndex)
    }

    override suspend fun getRecentSearches(limit: Int): List<String> = history.take(limit)
    override suspend fun clearHistory() { history.clear() }
}
