package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class SearchResponseDto(
    @SerializedName("results") val results: List<SearchResultDto>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("query") val query: String
)

data class SearchResultDto(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("snippet") val snippet: String,
    @SerializedName("highlight_ranges") val highlightRanges: List<List<Int>>,
    @SerializedName("source_id") val sourceId: Long,
    @SerializedName("relevance_score") val relevanceScore: Float
)
