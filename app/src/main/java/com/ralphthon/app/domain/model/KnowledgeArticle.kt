package com.ralphthon.app.domain.model

data class KnowledgeArticle(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val relevanceScore: Float
)
