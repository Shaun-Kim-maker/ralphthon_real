package com.ralphthon.app.domain.model

data class SearchResult(
    val id: Long,
    val type: String,
    val title: String,
    val snippet: String,
    val highlightRanges: List<IntRange>,
    val sourceId: Long,
    val relevanceScore: Float
)
