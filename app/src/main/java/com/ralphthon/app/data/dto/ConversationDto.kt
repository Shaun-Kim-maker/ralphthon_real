package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("customer_id") val customerId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("summary") val summary: String,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("keywords") val keywords: List<KeywordDto>,
    @SerializedName("key_statements") val keyStatements: List<KeyStatementDto>,
    @SerializedName("price_commitments") val priceCommitments: List<PriceCommitmentDto>,
    @SerializedName("action_items") val actionItems: List<ActionItemDto>,
    @SerializedName("predicted_questions") val predictedQuestions: List<PredictedQuestionDto>
)
