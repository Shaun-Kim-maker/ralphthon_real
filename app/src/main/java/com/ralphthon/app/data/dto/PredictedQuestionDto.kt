package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class PredictedQuestionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("question") val question: String,
    @SerializedName("suggested_answer") val suggestedAnswer: String,
    @SerializedName("related_knowledge") val relatedKnowledge: List<String>,
    @SerializedName("confidence") val confidence: Float
)
