package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.repository.KnowledgeRepository
import javax.inject.Inject

class GetKnowledgeUseCase @Inject constructor(
    private val repository: KnowledgeRepository
) {
    suspend fun getByCardId(cardId: Long): Result<List<KnowledgeArticle>> {
        return repository.getKnowledgeArticles(cardId)
    }

    suspend fun search(query: String): Result<List<KnowledgeArticle>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())
        return repository.searchKnowledge(trimmed)
    }

    suspend fun getByCardIdSorted(cardId: Long, sortBy: SortBy = SortBy.RELEVANCE): Result<List<KnowledgeArticle>> {
        return repository.getKnowledgeArticles(cardId).map { articles ->
            when (sortBy) {
                SortBy.RELEVANCE -> articles.sortedByDescending { it.relevanceScore }
                SortBy.TITLE -> articles.sortedBy { it.title }
                SortBy.CATEGORY -> articles.sortedBy { it.category }
            }
        }
    }

    enum class SortBy { RELEVANCE, TITLE, CATEGORY }
}
