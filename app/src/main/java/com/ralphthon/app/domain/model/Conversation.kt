package com.ralphthon.app.domain.model

data class Conversation(
    val id: Long,
    val customerId: Long,
    val title: String,
    val date: String,
    val type: ConversationType,
    val duration: Int,
    val summary: String,
    val sentiment: Sentiment,
    val keywords: List<Keyword>,
    val keyStatements: List<KeyStatement>,
    val priceCommitments: List<PriceCommitment>,
    val actionItems: List<ActionItem>,
    val predictedQuestions: List<PredictedQuestion>
)
