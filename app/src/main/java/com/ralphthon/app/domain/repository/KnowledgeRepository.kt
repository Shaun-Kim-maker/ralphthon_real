package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.KnowledgeArticle

interface KnowledgeRepository {
    suspend fun getKnowledgeArticles(cardId: Long): Result<List<KnowledgeArticle>>
    suspend fun searchKnowledge(query: String): Result<List<KnowledgeArticle>>
}
