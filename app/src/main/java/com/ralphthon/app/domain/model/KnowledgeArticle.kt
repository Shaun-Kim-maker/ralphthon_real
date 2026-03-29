package com.ralphthon.app.domain.model

data class KnowledgeArticle(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val relevanceScore: Float
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            title: String = "",
            content: String = "",
            category: String = "",
            relevanceScore: Float = 0f
        ): KnowledgeArticle {
            val resolvedContent = if (content.isBlank()) "(내용 없음)" else content
            return KnowledgeArticle(
                id = id,
                title = title,
                content = resolvedContent,
                category = category,
                relevanceScore = relevanceScore
            )
        }
    }
}
