package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.data.dto.KeyStatementDto
import com.ralphthon.app.data.dto.KeywordDto
import com.ralphthon.app.data.dto.KnowledgeDto
import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.KeywordCategory
import com.ralphthon.app.domain.model.Sentiment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CardMapperTest {

    private fun makeDto(
        id: Long = 1L,
        conversationId: Long = 10L,
        customerId: Long = 100L,
        title: String = "미팅 제목",
        date: String = "2026-03-01",
        conversationType: String = "CUSTOMER_MEETING",
        summary: String = "요약 내용",
        sentiment: String = "POSITIVE",
        sentimentScore: Float = 0.8f,
        keywords: List<KeywordDto> = emptyList(),
        keyStatements: List<KeyStatementDto> = emptyList(),
        priceCommitments: List<PriceCommitmentDto> = emptyList(),
        actionItems: List<ActionItemDto> = emptyList(),
        predictedQuestions: List<PredictedQuestionDto> = emptyList(),
        relatedKnowledge: List<KnowledgeDto> = emptyList()
    ) = CardDto(id, conversationId, customerId, title, date, conversationType, summary, sentiment,
        sentimentScore, keywords, keyStatements, priceCommitments, actionItems, predictedQuestions, relatedKnowledge)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 55L)
        val domain = CardMapper.toDomain(dto)
        assertEquals(55L, domain.id)
    }

    @Test
    fun should_mapConversationId_when_toDomain() {
        val dto = makeDto(conversationId = 20L)
        val domain = CardMapper.toDomain(dto)
        assertEquals(20L, domain.conversationId)
    }

    @Test
    fun should_mapCustomerId_when_toDomain() {
        val dto = makeDto(customerId = 200L)
        val domain = CardMapper.toDomain(dto)
        assertEquals(200L, domain.customerId)
    }

    @Test
    fun should_mapTitle_when_toDomain() {
        val dto = makeDto(title = "Q1 영업 미팅")
        val domain = CardMapper.toDomain(dto)
        assertEquals("Q1 영업 미팅", domain.title)
    }

    @Test
    fun should_mapDate_when_toDomain() {
        val dto = makeDto(date = "2026-02-15")
        val domain = CardMapper.toDomain(dto)
        assertEquals("2026-02-15", domain.date)
    }

    @Test
    fun should_mapConversationType_when_toDomain() {
        val dto = makeDto(conversationType = "INTERNAL_MEETING")
        val domain = CardMapper.toDomain(dto)
        assertEquals(ConversationType.INTERNAL_MEETING, domain.conversationType)
    }

    @Test
    fun should_mapSummary_when_toDomain() {
        val dto = makeDto(summary = "가격 협상 진행")
        val domain = CardMapper.toDomain(dto)
        assertEquals("가격 협상 진행", domain.summary)
    }

    @Test
    fun should_mapSentiment_when_toDomain() {
        val dto = makeDto(sentiment = "NEGATIVE")
        val domain = CardMapper.toDomain(dto)
        assertEquals(Sentiment.NEGATIVE, domain.sentiment)
    }

    @Test
    fun should_mapSentimentScore_when_toDomain() {
        val dto = makeDto(sentimentScore = 0.65f)
        val domain = CardMapper.toDomain(dto)
        assertEquals(0.65f, domain.sentimentScore)
    }

    @Test
    fun should_mapKeywords_when_toDomain() {
        val kwDto = KeywordDto(text = "가격", category = "PRICE", frequency = 3)
        val dto = makeDto(keywords = listOf(kwDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.keywords.size)
        assertEquals("가격", domain.keywords[0].text)
        assertEquals(3, domain.keywords[0].frequency)
    }

    @Test
    fun should_mapKeywordCategory_when_toDomain() {
        val kwDto = KeywordDto(text = "경쟁사", category = "COMPETITOR", frequency = 1)
        val dto = makeDto(keywords = listOf(kwDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(KeywordCategory.COMPETITOR, domain.keywords[0].category)
    }

    @Test
    fun should_mapKeyStatements_when_toDomain() {
        val ksDto = KeyStatementDto(id = 1L, speaker = "고객", text = "가격이 높다", timestamp = "00:05", sentiment = "NEGATIVE", isImportant = true)
        val dto = makeDto(keyStatements = listOf(ksDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.keyStatements.size)
        assertEquals("고객", domain.keyStatements[0].speaker)
        assertEquals("가격이 높다", domain.keyStatements[0].text)
        assertEquals(true, domain.keyStatements[0].isImportant)
    }

    @Test
    fun should_mapKeyStatementSentiment_when_toDomain() {
        val ksDto = KeyStatementDto(id = 2L, speaker = "영업", text = "협상 가능", timestamp = "00:10", sentiment = "POSITIVE", isImportant = false)
        val dto = makeDto(keyStatements = listOf(ksDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(Sentiment.POSITIVE, domain.keyStatements[0].sentiment)
    }

    @Test
    fun should_mapPriceCommitments_when_toDomain() {
        val pcDto = PriceCommitmentDto(id = 1L, amount = 5000000.0, currency = "KRW", condition = "3개월 계약", mentionedAt = "00:30")
        val dto = makeDto(priceCommitments = listOf(pcDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.priceCommitments.size)
        assertEquals(5000000.0, domain.priceCommitments[0].amount)
        assertEquals("KRW", domain.priceCommitments[0].currency)
    }

    @Test
    fun should_mapActionItems_when_toDomain() {
        val aiDto = ActionItemDto(id = 1L, description = "견적서 발송", assignee = "김영업", dueDate = "2026-03-10", status = "OPEN")
        val dto = makeDto(actionItems = listOf(aiDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.actionItems.size)
        assertEquals("견적서 발송", domain.actionItems[0].description)
        assertEquals("김영업", domain.actionItems[0].assignee)
    }

    @Test
    fun should_mapActionItemStatus_when_toDomain() {
        val aiDto = ActionItemDto(id = 2L, description = "계약서 검토", assignee = "법무팀", dueDate = null, status = "DONE")
        val dto = makeDto(actionItems = listOf(aiDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(ActionItemStatus.DONE, domain.actionItems[0].status)
    }

    @Test
    fun should_mapPredictedQuestions_when_toDomain() {
        val pqDto = PredictedQuestionDto(id = 1L, question = "납기는?", suggestedAnswer = "2주 내 가능", relatedKnowledge = listOf("배송정책"), confidence = 0.9f)
        val dto = makeDto(predictedQuestions = listOf(pqDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.predictedQuestions.size)
        assertEquals("납기는?", domain.predictedQuestions[0].question)
        assertEquals(0.9f, domain.predictedQuestions[0].confidence)
    }

    @Test
    fun should_mapRelatedKnowledge_when_toDomain() {
        val knDto = KnowledgeDto(id = 1L, title = "제품 스펙", content = "상세 내용", category = "PRODUCT", relevanceScore = 0.75f)
        val dto = makeDto(relatedKnowledge = listOf(knDto))
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.relatedKnowledge.size)
        assertEquals("제품 스펙", domain.relatedKnowledge[0].title)
        assertEquals(0.75f, domain.relatedKnowledge[0].relevanceScore)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = CardMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_handleEmptyNestedLists_when_toDomain() {
        val dto = makeDto(keywords = emptyList(), keyStatements = emptyList(), priceCommitments = emptyList(),
            actionItems = emptyList(), predictedQuestions = emptyList(), relatedKnowledge = emptyList())
        val domain = CardMapper.toDomain(dto)
        assertEquals(0, domain.keywords.size)
        assertEquals(0, domain.keyStatements.size)
        assertEquals(0, domain.priceCommitments.size)
        assertEquals(0, domain.actionItems.size)
        assertEquals(0, domain.predictedQuestions.size)
        assertEquals(0, domain.relatedKnowledge.size)
    }
}
