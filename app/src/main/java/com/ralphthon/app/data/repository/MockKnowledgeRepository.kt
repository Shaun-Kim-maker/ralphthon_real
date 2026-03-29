package com.ralphthon.app.data.repository

import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.repository.KnowledgeRepository
import javax.inject.Inject

class MockKnowledgeRepository @Inject constructor() : KnowledgeRepository {

    override suspend fun getKnowledgeArticles(cardId: Long): Result<List<KnowledgeArticle>> {
        return Result.success(emptyList())
    }

    override suspend fun searchKnowledge(query: String): Result<List<KnowledgeArticle>> {
        return Result.success(emptyList())
    }
}
