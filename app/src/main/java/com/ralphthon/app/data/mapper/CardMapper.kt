package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.data.dto.KeyStatementDto
import com.ralphthon.app.data.dto.KeywordDto
import com.ralphthon.app.data.dto.KnowledgeDto
import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Keyword
import com.ralphthon.app.domain.model.KeywordCategory
import com.ralphthon.app.domain.model.KeyStatement
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment

object CardMapper {
    fun toDomain(dto: CardDto): ContextCard {
        return ContextCard(
            id = dto.id,
            conversationId = dto.conversationId,
            customerId = dto.customerId,
            title = dto.title,
            date = dto.date,
            conversationType = ConversationType.valueOf(dto.conversationType),
            summary = dto.summary,
            sentiment = Sentiment.fromString(dto.sentiment),
            sentimentScore = dto.sentimentScore,
            keywords = dto.keywords.map { mapKeyword(it) },
            keyStatements = dto.keyStatements.map { mapKeyStatement(it) },
            priceCommitments = dto.priceCommitments.map { mapPriceCommitment(it) },
            actionItems = dto.actionItems.map { mapActionItem(it) },
            predictedQuestions = dto.predictedQuestions.map { mapPredictedQuestion(it) },
            relatedKnowledge = dto.relatedKnowledge.map { mapKnowledge(it) }
        )
    }

    fun toDomainList(dtos: List<CardDto>): List<ContextCard> = dtos.map { toDomain(it) }

    private fun mapKeyword(dto: KeywordDto): Keyword = Keyword(
        text = dto.text,
        category = KeywordCategory.fromString(dto.category),
        frequency = dto.frequency
    )

    private fun mapKeyStatement(dto: KeyStatementDto): KeyStatement = KeyStatement(
        id = dto.id,
        speaker = dto.speaker,
        text = dto.text,
        timestamp = dto.timestamp,
        sentiment = Sentiment.fromString(dto.sentiment),
        isImportant = dto.isImportant
    )

    private fun mapPriceCommitment(dto: PriceCommitmentDto): PriceCommitment = PriceCommitment(
        id = dto.id,
        amount = dto.amount,
        currency = dto.currency,
        condition = dto.condition,
        mentionedAt = dto.mentionedAt
    )

    private fun mapActionItem(dto: ActionItemDto): ActionItem = ActionItem(
        id = dto.id,
        description = dto.description,
        assignee = dto.assignee,
        dueDate = dto.dueDate,
        status = ActionItemStatus.valueOf(dto.status)
    )

    private fun mapPredictedQuestion(dto: PredictedQuestionDto): PredictedQuestion = PredictedQuestion(
        id = dto.id,
        question = dto.question,
        suggestedAnswer = dto.suggestedAnswer,
        relatedKnowledge = dto.relatedKnowledge,
        confidence = dto.confidence
    )

    private fun mapKnowledge(dto: KnowledgeDto): KnowledgeArticle = KnowledgeArticle(
        id = dto.id,
        title = dto.title,
        content = dto.content,
        category = dto.category,
        relevanceScore = dto.relevanceScore
    )
}
