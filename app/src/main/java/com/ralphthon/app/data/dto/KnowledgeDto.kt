package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class KnowledgeDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("relevance_score") val relevanceScore: Float
)
