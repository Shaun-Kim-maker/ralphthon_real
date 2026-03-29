package com.ralphthon.app.domain.model

data class PredictedQuestion(
    val id: Long,
    val question: String,
    val suggestedAnswer: String,
    val relatedKnowledge: List<String>,
    val confidence: Float
)
