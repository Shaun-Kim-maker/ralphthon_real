package com.ralphthon.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModelEqualityTest {

    // Tests 1-2: Customer equality
    @Test
    fun should_beEqual_when_sameCustomerId() {
        val c1 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        val c2 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        assertEquals(c1, c2)
    }

    @Test
    fun should_notBeEqual_when_differentCustomerId() {
        val c1 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        val c2 = Customer(2L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        assertNotEquals(c1, c2)
    }

    // Tests 3-4: ContextCard equality
    @Test
    fun should_beEqual_when_sameCardId() {
        val card1 = ContextCard(1L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val card2 = ContextCard(1L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(card1, card2)
    }

    @Test
    fun should_notBeEqual_when_differentCardId() {
        val card1 = ContextCard(1L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val card2 = ContextCard(2L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertNotEquals(card1, card2)
    }

    // Tests 5-6: copy()
    @Test
    fun should_copyWithNewName_when_copyUsed() {
        val customer = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        val copied = customer.copy(contactName = "새이름")
        assertEquals("새이름", copied.contactName)
        assertEquals(customer.id, copied.id)
        assertEquals(customer.companyName, copied.companyName)
    }

    @Test
    fun should_copyWithNewSummary_when_copyUsed() {
        val card = ContextCard(1L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "원래요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val copied = card.copy(summary = "새요약")
        assertEquals("새요약", copied.summary)
        assertEquals(card.id, copied.id)
    }

    // Tests 7-8: hashCode
    @Test
    fun should_hashEqual_when_sameId() {
        val c1 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        val c2 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    @Test
    fun should_hashDiffer_when_differentId() {
        val c1 = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        val c2 = Customer(2L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        assertNotEquals(c1.hashCode(), c2.hashCode())
    }

    // Tests 9-10: toString
    @Test
    fun should_toString_when_customerCreated() {
        val customer = Customer(1L, "삼성", "홍길동", "IT", "2024-01-01", 5, null)
        assertTrue(customer.toString().contains("삼성"))
    }

    @Test
    fun should_toString_when_cardCreated() {
        val card = ContextCard(1L, 10L, 100L, "회의", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, "요약", Sentiment.NEUTRAL,
            0.5f, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertTrue(card.toString().contains("회의"))
    }

    // Tests 11-12: KeyStatement equality
    @Test
    fun should_beEqual_when_sameKeyStatementFields() {
        val ks1 = KeyStatement(1L, "홍길동", "중요한 발언", "01:00", Sentiment.POSITIVE, true)
        val ks2 = KeyStatement(1L, "홍길동", "중요한 발언", "01:00", Sentiment.POSITIVE, true)
        assertEquals(ks1, ks2)
    }

    @Test
    fun should_notBeEqual_when_differentKeyStatementText() {
        val ks1 = KeyStatement(1L, "홍길동", "발언A", "01:00", Sentiment.POSITIVE, true)
        val ks2 = KeyStatement(1L, "홍길동", "발언B", "01:00", Sentiment.POSITIVE, true)
        assertNotEquals(ks1, ks2)
    }

    // Tests 13-14: Keyword equality and copy
    @Test
    fun should_beEqual_when_sameKeywordFields() {
        val k1 = Keyword("가격", KeywordCategory.PRICE, 5)
        val k2 = Keyword("가격", KeywordCategory.PRICE, 5)
        assertEquals(k1, k2)
    }

    @Test
    fun should_copyKeyword_when_frequencyChanged() {
        val keyword = Keyword("가격", KeywordCategory.PRICE, 5)
        val copied = keyword.copy(frequency = 10)
        assertEquals(10, copied.frequency)
        assertEquals(keyword.text, copied.text)
        assertEquals(keyword.category, copied.category)
    }

    // Tests 15-17: PriceCommitment equality and copy
    @Test
    fun should_beEqual_when_samePriceCommitmentFields() {
        val pc1 = PriceCommitment(1L, 100.0, "KRW", "조건없음", "2024-01-01")
        val pc2 = PriceCommitment(1L, 100.0, "KRW", "조건없음", "2024-01-01")
        assertEquals(pc1, pc2)
    }

    @Test
    fun should_notBeEqual_when_differentAmount() {
        val pc1 = PriceCommitment(1L, 100.0, "KRW", "조건없음", "2024-01-01")
        val pc2 = PriceCommitment(1L, 200.0, "KRW", "조건없음", "2024-01-01")
        assertNotEquals(pc1, pc2)
    }

    @Test
    fun should_copyPriceCommitment_when_amountChanged() {
        val pc = PriceCommitment(1L, 100.0, "KRW", "조건없음", "2024-01-01")
        val copied = pc.copy(amount = 500.0)
        assertEquals(500.0, copied.amount)
        assertEquals(pc.id, copied.id)
        assertEquals(pc.currency, copied.currency)
    }

    // Tests 18-20: ActionItem equality and copy
    @Test
    fun should_beEqual_when_sameActionItemFields() {
        val ai1 = ActionItem(1L, "제안서 작성", "홍길동", "2024-02-01", ActionItemStatus.OPEN)
        val ai2 = ActionItem(1L, "제안서 작성", "홍길동", "2024-02-01", ActionItemStatus.OPEN)
        assertEquals(ai1, ai2)
    }

    @Test
    fun should_notBeEqual_when_differentActionItemStatus() {
        val ai1 = ActionItem(1L, "제안서 작성", "홍길동", "2024-02-01", ActionItemStatus.OPEN)
        val ai2 = ActionItem(1L, "제안서 작성", "홍길동", "2024-02-01", ActionItemStatus.DONE)
        assertNotEquals(ai1, ai2)
    }

    @Test
    fun should_copyActionItem_when_statusChanged() {
        val ai = ActionItem(1L, "제안서 작성", "홍길동", "2024-02-01", ActionItemStatus.OPEN)
        val copied = ai.copy(status = ActionItemStatus.DONE)
        assertEquals(ActionItemStatus.DONE, copied.status)
        assertEquals(ai.id, copied.id)
        assertEquals(ai.description, copied.description)
    }

    // Tests 21-22: PredictedQuestion equality and copy
    @Test
    fun should_beEqual_when_samePredictedQuestionFields() {
        val pq1 = PredictedQuestion(1L, "가격 질문?", "답변입니다", listOf("지식1"), 0.8f)
        val pq2 = PredictedQuestion(1L, "가격 질문?", "답변입니다", listOf("지식1"), 0.8f)
        assertEquals(pq1, pq2)
    }

    @Test
    fun should_copyPredictedQuestion_when_confidenceChanged() {
        val pq = PredictedQuestion(1L, "가격 질문?", "답변입니다", listOf("지식1"), 0.8f)
        val copied = pq.copy(confidence = 0.9f)
        assertEquals(0.9f, copied.confidence)
        assertEquals(pq.id, copied.id)
        assertEquals(pq.question, copied.question)
    }

    // Test 23: SearchResult equality
    @Test
    fun should_beEqual_when_sameSearchResultFields() {
        val sr1 = SearchResult(1L, "CARD", "검색결과", "스니펫", emptyList(), 100L, 0.95f)
        val sr2 = SearchResult(1L, "CARD", "검색결과", "스니펫", emptyList(), 100L, 0.95f)
        assertEquals(sr1, sr2)
    }

    // Test 24: Conversation equality
    @Test
    fun should_beEqual_when_sameConversationFields() {
        val conv1 = Conversation(1L, 10L, "미팅", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, 60, "요약", Sentiment.POSITIVE,
            emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val conv2 = Conversation(1L, 10L, "미팅", "2024-01-01",
            ConversationType.CUSTOMER_MEETING, 60, "요약", Sentiment.POSITIVE,
            emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(conv1, conv2)
    }

    // Test 25: Contact equality
    @Test
    fun should_beEqual_when_sameContactFields() {
        val contact1 = Contact(1L, 10L, "홍길동", "매니저", "hong@test.com", "010-1234-5678")
        val contact2 = Contact(1L, 10L, "홍길동", "매니저", "hong@test.com", "010-1234-5678")
        assertEquals(contact1, contact2)
    }
}
