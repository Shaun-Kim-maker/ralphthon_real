package com.ralphthon.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ModelValidationTest {

    // ─── Customer ───────────────────────────────────────────────────────────────

    @Test
    fun should_createCustomer_when_allFieldsValid() {
        val customer = Customer(
            id = 1L,
            companyName = "Acme",
            contactName = "홍길동",
            industry = "IT",
            lastInteractionDate = "2026-03-01",
            totalConversations = 5,
            summary = null
        )
        assertNotNull(customer)
    }

    @Test
    fun should_defaultName_when_nameIsBlank() {
        val customer = Customer.withDefaults(name = "")
        assertEquals("이름 없음", customer.contactName ?: "이름 없음")
    }

    @Test
    fun should_truncateName_when_nameExceeds50() {
        val longName = "가".repeat(60)
        val customer = Customer.withDefaults(name = longName)
        assertEquals(50, customer.contactName!!.length)
    }

    @Test
    fun should_defaultCompany_when_companyIsBlank() {
        val customer = Customer.withDefaults(company = "")
        assertEquals("회사 미등록", customer.companyName)
    }

    // ─── ContextCard ─────────────────────────────────────────────────────────────

    @Test
    fun should_createCard_when_allFieldsValid() {
        val card = ContextCard(
            id = 1L,
            conversationId = 1L,
            customerId = 1L,
            title = "미팅",
            date = "2026-03-01",
            conversationType = ConversationType.CUSTOMER_MEETING,
            summary = "요약",
            sentiment = Sentiment.POSITIVE,
            sentimentScore = 0.8f,
            keywords = emptyList(),
            keyStatements = emptyList(),
            priceCommitments = emptyList(),
            actionItems = emptyList(),
            predictedQuestions = emptyList(),
            relatedKnowledge = emptyList()
        )
        assertNotNull(card)
    }

    @Test
    fun should_truncateTitle_when_titleExceeds80() {
        val longTitle = "가".repeat(100)
        val card = ContextCard.withDefaults(title = longTitle)
        assertEquals(80, card.title.length)
    }

    @Test
    fun should_truncateSummary_when_summaryExceeds300() {
        val longSummary = "가".repeat(400)
        val card = ContextCard.withDefaults(summary = longSummary)
        assertEquals(300, card.summary.length)
    }

    // ─── KeyStatement ────────────────────────────────────────────────────────────

    @Test
    fun should_createKeyStatement_when_valid() {
        val ks = KeyStatement(
            id = 1L,
            speaker = "김철수",
            text = "가격 논의",
            timestamp = "01:30",
            sentiment = Sentiment.NEUTRAL,
            isImportant = false
        )
        assertNotNull(ks)
    }

    @Test
    fun should_handleNegativeTimestamp_when_invalid() {
        val ks = KeyStatement.withDefaults(timestampSeconds = -1)
        assertEquals("00:00", ks.timestamp)
    }

    // ─── Keyword ─────────────────────────────────────────────────────────────────

    @Test
    fun should_createKeyword_when_valid() {
        val kw = Keyword(text = "가격", category = KeywordCategory.PRICE, frequency = 3)
        assertNotNull(kw)
    }

    @Test
    fun should_handleEmptyTerm_when_termBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            Keyword.withDefaults(term = "")
        }
    }

    // ─── KnowledgeArticle ────────────────────────────────────────────────────────

    @Test
    fun should_createKnowledgeArticle_when_valid() {
        val article = KnowledgeArticle(
            id = 1L,
            title = "제품 소개",
            content = "내용",
            category = "PRODUCT",
            relevanceScore = 0.9f
        )
        assertNotNull(article)
    }

    @Test
    fun should_handleEmptyContent_when_contentBlank() {
        val article = KnowledgeArticle.withDefaults(content = "")
        assertEquals("(내용 없음)", article.content)
    }

    // ─── Sentiment ───────────────────────────────────────────────────────────────

    @Test
    fun should_parseSentimentFromString_when_valid() {
        assertEquals(Sentiment.POSITIVE, Sentiment.fromString("POSITIVE"))
    }

    @Test
    fun should_defaultSentiment_when_unknownString() {
        assertEquals(Sentiment.NEUTRAL, Sentiment.fromString("INVALID"))
    }

    // ─── KeywordCategory ─────────────────────────────────────────────────────────

    @Test
    fun should_parseCategoryFromString_when_valid() {
        assertEquals(KeywordCategory.PRODUCT, KeywordCategory.fromString("PRODUCT"))
    }

    // ─── PriceCommitment ─────────────────────────────────────────────────────────

    @Test
    fun should_createPriceCommitment_when_allFieldsValid() {
        val pc = PriceCommitment(
            id = 1L,
            amount = 500000.0,
            currency = "KRW",
            condition = "선결제",
            mentionedAt = "02:15"
        )
        assertNotNull(pc)
    }

    @Test
    fun should_handleZeroAmount_when_amountIsZero() {
        val pc = PriceCommitment.withDefaults(amount = 0.0)
        assertEquals(0.0, pc.amount)
    }

    @Test
    fun should_handleNegativeAmount_when_amountNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            PriceCommitment.withDefaults(amount = -100.0)
        }
    }

    @Test
    fun should_defaultCurrency_when_currencyBlank() {
        val pc = PriceCommitment.withDefaults(currency = "")
        assertEquals("KRW", pc.currency)
    }

    @Test
    fun should_handleEmptyCondition_when_conditionBlank() {
        val pc = PriceCommitment.withDefaults(condition = "")
        assertEquals("(조건 없음)", pc.condition)
    }

    @Test
    fun should_handleLargeAmount_when_amountExceedsMax() {
        val largeAmount = 1_000_000_000_000.0
        val pc = PriceCommitment.withDefaults(amount = largeAmount)
        assertEquals(largeAmount, pc.amount)
    }

    @Test
    fun should_trimCurrency_when_currencyHasSpaces() {
        val pc = PriceCommitment.withDefaults(currency = " USD ")
        assertEquals("USD", pc.currency)
    }

    @Test
    fun should_upperCaseCurrency_when_lowercase() {
        val pc = PriceCommitment.withDefaults(currency = "usd")
        assertEquals("USD", pc.currency)
    }

    // ─── ActionItem ──────────────────────────────────────────────────────────────

    @Test
    fun should_createActionItem_when_allFieldsValid() {
        val ai = ActionItem(
            id = 1L,
            description = "계약서 발송",
            assignee = "김영업",
            dueDate = "2026-04-01",
            status = ActionItemStatus.OPEN
        )
        assertNotNull(ai)
    }

    @Test
    fun should_defaultStatusOpen_when_created() {
        val ai = ActionItem.withDefaults(description = "태스크")
        assertEquals(ActionItemStatus.OPEN, ai.status)
    }

    @Test
    fun should_transitionToDone_when_statusChanged() {
        val ai = ActionItem.withDefaults(description = "태스크")
        val done = ai.copy(status = ActionItemStatus.DONE)
        assertEquals(ActionItemStatus.DONE, done.status)
    }

    @Test
    fun should_handleNullDueDate_when_noDueDate() {
        val ai = ActionItem.withDefaults(description = "태스크", dueDate = null)
        assertNull(ai.dueDate)
    }

    @Test
    fun should_handleEmptyDescription_when_descBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            ActionItem.withDefaults(description = "")
        }
    }

    @Test
    fun should_handleEmptyAssignee_when_assigneeBlank() {
        val ai = ActionItem.withDefaults(description = "태스크", assignee = "")
        assertEquals("(미지정)", ai.assignee)
    }

    @Test
    fun should_trimDescription_when_hasWhitespace() {
        val ai = ActionItem.withDefaults(description = " task ")
        assertEquals("task", ai.description)
    }

    @Test
    fun should_handleLongDescription_when_exceeds200() {
        val longDesc = "가".repeat(300)
        val ai = ActionItem.withDefaults(description = longDesc)
        assertEquals(200, ai.description.length)
    }

    // ─── PredictedQuestion ───────────────────────────────────────────────────────

    @Test
    fun should_createPredictedQuestion_when_allFieldsValid() {
        val pq = PredictedQuestion(
            id = 1L,
            question = "가격은?",
            suggestedAnswer = "협의 가능합니다",
            relatedKnowledge = listOf("가격표"),
            confidence = 0.85f
        )
        assertNotNull(pq)
    }

    @Test
    fun should_clampConfidence_when_below0() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", confidence = -0.5f)
        assertEquals(0.0f, pq.confidence)
    }

    @Test
    fun should_clampConfidence_when_above1() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", confidence = 1.5f)
        assertEquals(1.0f, pq.confidence)
    }

    @Test
    fun should_handleEmptyQuestion_when_questionBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            PredictedQuestion.withDefaults(question = "")
        }
    }

    @Test
    fun should_handleEmptySuggestedAnswer_when_blank() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", suggestedAnswer = "")
        assertEquals("(답변 준비 중)", pq.suggestedAnswer)
    }

    @Test
    fun should_handleEmptyRelatedKnowledge_when_empty() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", relatedKnowledge = emptyList())
        assertNotNull(pq)
        assertEquals(0, pq.relatedKnowledge.size)
    }

    @Test
    fun should_handleMaxConfidence_when_exactly1() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", confidence = 1.0f)
        assertEquals(1.0f, pq.confidence)
    }

    @Test
    fun should_handleMinConfidence_when_exactly0() {
        val pq = PredictedQuestion.withDefaults(question = "질문?", confidence = 0.0f)
        assertEquals(0.0f, pq.confidence)
    }
}
