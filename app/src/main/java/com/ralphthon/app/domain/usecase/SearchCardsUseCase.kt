package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.repository.BriefRepository
import javax.inject.Inject

class SearchCardsUseCase @Inject constructor(
    private val repository: BriefRepository
) {
    suspend operator fun invoke(query: String): Result<List<SearchResult>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return Result.success(emptyList())
        }
        return repository.search(trimmedQuery)
    }

    suspend fun searchSorted(query: String, sortBy: SortBy = SortBy.RELEVANCE): Result<List<SearchResult>> {
        return invoke(query).map { results ->
            when (sortBy) {
                SortBy.RELEVANCE -> results.sortedByDescending { it.relevanceScore }
                SortBy.TITLE -> results.sortedBy { it.title }
            }
        }
    }

    suspend fun searchFiltered(query: String, type: String? = null): Result<List<SearchResult>> {
        return invoke(query).map { results ->
            if (type != null) {
                results.filter { it.type == type }
            } else {
                results
            }
        }
    }

    enum class SortBy { RELEVANCE, TITLE }
}
