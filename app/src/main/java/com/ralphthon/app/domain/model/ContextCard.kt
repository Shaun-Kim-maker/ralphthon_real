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
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            conversationId: Long = 0L,
            customerId: Long = 0L,
            title: String = "",
            date: String = "",
            conversationType: ConversationType = ConversationType.CUSTOMER_MEETING,
            summary: String = "",
            sentiment: Sentiment = Sentiment.NEUTRAL,
            sentimentScore: Float = 0f,
            keywords: List<Keyword> = emptyList(),
            keyStatements: List<KeyStatement> = emptyList(),
            priceCommitments: List<PriceCommitment> = emptyList(),
            actionItems: List<ActionItem> = emptyList(),
            predictedQuestions: List<PredictedQuestion> = emptyList(),
            relatedKnowledge: List<KnowledgeArticle> = emptyList()
        ): ContextCard {
            val resolvedTitle = if (title.length > 80) title.take(80) else title
            val resolvedSummary = if (summary.length > 300) summary.take(300) else summary
            return ContextCard(
                id = id,
                conversationId = conversationId,
                customerId = customerId,
                title = resolvedTitle,
                date = date,
                conversationType = conversationType,
                summary = resolvedSummary,
                sentiment = sentiment,
                sentimentScore = sentimentScore,
                keywords = keywords,
                keyStatements = keyStatements,
                priceCommitments = priceCommitments,
                actionItems = actionItems,
                predictedQuestions = predictedQuestions,
                relatedKnowledge = relatedKnowledge
            )
        }
    }
}
