package com.ralphthon.app.domain.model

data class ContextCard(
    val id: Long,
    val conversationId: Long,
    val customerId: Long,
    val title: String,
    val date: String,
    val conversationType: ConversationType,
    val summary: String,
    val sentiment: Sentiment,
    val sentimentScore: Float,
    val keywords: List<Keyword>,
    val keyStatements: List<KeyStatement>,
    val priceCommitments: List<PriceCommitment>,
    val actionItems: List<ActionItem>,
    val predictedQuestions: List<PredictedQuestion>,
    val relatedKnowledge: List<KnowledgeArticle>
)
