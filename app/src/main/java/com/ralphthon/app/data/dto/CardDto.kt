package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class CardDto(
    @SerializedName("id") val id: Long,
    @SerializedName("conversation_id") val conversationId: Long,
    @SerializedName("customer_id") val customerId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("conversation_type") val conversationType: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("sentiment_score") val sentimentScore: Float,
    @SerializedName("keywords") val keywords: List<KeywordDto>,
    @SerializedName("key_statements") val keyStatements: List<KeyStatementDto>,
    @SerializedName("price_commitments") val priceCommitments: List<PriceCommitmentDto>,
    @SerializedName("action_items") val actionItems: List<ActionItemDto>,
    @SerializedName("predicted_questions") val predictedQuestions: List<PredictedQuestionDto>,
    @SerializedName("related_knowledge") val relatedKnowledge: List<KnowledgeDto>
)

data class KeyStatementDto(
    @SerializedName("id") val id: Long,
    @SerializedName("speaker") val speaker: String,
    @SerializedName("text") val text: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("is_important") val isImportant: Boolean
)

data class KeywordDto(
    @SerializedName("text") val text: String,
    @SerializedName("category") val category: String,
    @SerializedName("frequency") val frequency: Int
)
